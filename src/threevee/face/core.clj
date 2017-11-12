(ns threevee.face.core
  (:require
   [boot.core :as c :refer [deftask]]
   [clojure.java.io :as io]
   [threevee.face.extractor :as extract]
   [threevee.face.detector :as detect]
   [threevee.image.core :as img]
   [threevee.input.core :as inpt])
  (:import
   [java.io File]))

(defn indexed-face-rects [face-img detector-config]
  (map-indexed
   (fn [i rect] [i rect])
   (detect/detect-faces face-img detector-config)))

(defn indexed-input-files [input-files detector-config]
  (->> input-files
       (map-indexed
        (fn [i tfile]
          (let [[input-img-name path] (inpt/tmpfile->name tfile)
                face-img (img/image-by-path path)
                idx-face-rects (indexed-face-rects face-img detector-config)]
            [i input-img-name face-img idx-face-rects])))))

(defn output-path [root img-idx rect-idx name]
  (let [file-tag (format "%04d" img-idx)
        rect-tag (format "%02d" rect-idx)]
    (str root
         "/" extract/OUTPUT-ART-DIR
         "/" file-tag
         "_" rect-tag
         "_" name)))

(deftask extract-faces []
  (let [tmp (c/tmp-dir!)]
    (println "extracting faces...")
    (fn middleware [next-handler]
      (fn handler [fileset]
        (c/empty-dir! tmp)
        (let [input-files (extract/face-input-files fileset)
              detector (detect/haar-face-detector fileset)
              detector-config (detect/detector-config detector)
              idx-input-files (indexed-input-files input-files detector-config)
              root (.getPath tmp)]
          (doseq [[img-idx input-img-name face-img idx-face-rects] idx-input-files]
            (doseq [[rect-idx rect] idx-face-rects]
              (let [result-path (output-path root
                                             (inc img-idx)
                                             (inc rect-idx)
                                             input-img-name)
                    resized (img/resize-by-rect face-img rect)]
                (println "width: " (.-width rect))
                (println "result-path: " result-path)
                (println "make-parents: " (clojure.java.io/make-parents result-path))
                (println "file written: " (img/save-to-path resized result-path)))))
          (-> fileset
              (c/add-asset tmp)
              c/commit!
              next-handler))))))
