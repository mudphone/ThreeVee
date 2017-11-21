(ns threevee.face.core
  (:require
   [boot.core :as c :refer [deftask]]
   [threevee.detector.core :as det-cfg]
   [threevee.face.detector :as detect]
   [threevee.face.extractor :as extract]
   [threevee.face.preprocess :as prep]
   [threevee.input.core :as inpt]))

(defn extract-faces [type-str input-files-fn out-local]
  (let [tmp (c/tmp-dir!)]
    (println "extracting faces from" type-str "...")
    (fn middleware [next-handler]
      (fn handler [fileset]
        (c/empty-dir! tmp)
        (let [input-files (input-files-fn fileset)
              detector (det-cfg/haar-face-detector fileset)
              detector-config (det-cfg/detector-config detector)]
          (extract/extract-faces input-files
                                 (.getPath tmp)
                                 out-local
                                 detector-config))
        (-> fileset
            (c/add-asset tmp)
            c/commit!
            next-handler)))))

(deftask extract-guest-faces []
  (extract-faces "guests"
                 inpt/guest-input-files
                 inpt/OUTPUT-GUEST-FACE-EXTRACTIONS-DIR))

(deftask extract-art-faces []
  (extract-faces "artwork"
                 inpt/art-input-files
                 inpt/OUTPUT-ART-FACE-EXTRACTIONS-DIR))

(defn mark-faces [type-str input-files-fn out-local]
  (let [tmp (c/tmp-dir!)]
    (println "marking faces in" type-str "...")
    (fn middleware [next-handler]
      (fn handler [fileset]
        (c/empty-dir! tmp)
        (let [input-files (input-files-fn fileset)
              detector (det-cfg/haar-face-detector fileset)
              detector-config (det-cfg/detector-config detector)]
          (detect/detect-and-draw-faces input-files
                                        (.getPath tmp)
                                        out-local
                                        detector-config))
        (-> fileset
            (c/add-asset tmp)
            c/commit!
            next-handler)))))

(deftask mark-art-faces []
  (mark-faces "artwork"
              inpt/art-input-files
              inpt/OUTPUT-ART-FACE-DETECTIONS-DIR))

(defn output-path
  [{:keys [image-index image-name]} root local]
  )

(defn preprocess-faces [type-str input-files-fn out-local]
  (let [tmp (c/tmp-dir!)]
    (println "preprocessing" type-str "faces...")
    (fn middleware [next-handler]
      (fn handler [fileset]
        (c/empty-dir! tmp)
        (->> (input-files-fn fileset)
             (map c/tmp-file)
             inpt/indexed-images
             prep/preprocess
             (map (fn [{:keys [image-index image-name] :as info}]
                    (let [file-tag (format "%04d" image-index)
                          path (str (.getPath tmp)
                                    "/" out-local
                                    "/" file-tag
                                    "__" image-name)]
                      (merge info {:output-path path}))))
             inpt/save-image-to-output-path)
        (-> fileset
            (c/add-asset tmp)
            c/commit!
            next-handler)))))

(deftask preprocess-guest-faces []
  (preprocess-faces "guest"
                    inpt/guest-cropped-face-input-files
                    inpt/OUTPUT-PREPROCESSED-GUEST-FACE-DIR))
