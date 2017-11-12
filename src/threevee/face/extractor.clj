(ns threevee.face.extractor
  (:require
   [clojure.java.io :as io]
   [threevee.face.detector :as detect]
   [threevee.image.core :as img]
   [threevee.input.core :as inpt]))

(defn- output-path [root img-idx rect-idx name]
  (let [file-tag (format "%04d" img-idx)
        rect-tag (format "%02d" rect-idx)]
    (str root
         "/" inpt/OUTPUT-FACE-EXTRACTIONS-DIR
         "/" file-tag
         "_" rect-tag
         "_" name)))

(defn extract-face-from-image [outdir-path img-idx input-img-name face-img idx-face-rects]
  (doseq [[rect-idx rect] idx-face-rects]
    (let [result-path (output-path outdir-path
                                   (inc img-idx)
                                   (inc rect-idx)
                                   input-img-name)
          resized (img/resize-by-rect face-img rect)]
      (println "width: " (.-width rect))
      (println "result-path: " result-path)
      (println "make-parents: " (io/make-parents result-path))
      (println "file written: " (img/save-to-path resized result-path)))))

(defn extract-faces [input-files outdir-path detector-config]
  (let [idx-input-files (detect/indexed-input-files input-files detector-config)]
    (count
     (pmap (fn [[img-idx input-img-name face-img idx-face-rects]]
             (extract-face-from-image
              outdir-path
              img-idx input-img-name face-img idx-face-rects))
           idx-input-files))))
