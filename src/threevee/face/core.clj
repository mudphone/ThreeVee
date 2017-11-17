(ns threevee.face.core
  (:require
   [boot.core :as c :refer [deftask]]
   [threevee.face.detector :as detect]
   [threevee.face.extractor :as extract]
   [threevee.input.core :as inpt]))

(defn extract-faces [type-str input-files-fn out-local]
  (let [tmp (c/tmp-dir!)]
    (println "extracting faces from" type-str "...")
    (fn middleware [next-handler]
      (fn handler [fileset]
        (c/empty-dir! tmp)
        (let [input-files (input-files-fn fileset)
              detector (detect/haar-face-detector fileset)
              detector-config (detect/detector-config detector)]
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
              detector (detect/haar-face-detector fileset)
              detector-config (detect/detector-config detector)]
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
