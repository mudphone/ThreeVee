(ns threevee.face.extractor
  (:require
   [boot.core :as c :refer [deftask]]
   [clojure.java.io :as io]
   [threevee.face.detector :as detect]
   [threevee.image.core :as img]
   [threevee.input.core :as inpt]))

(def INPUT-ROOT-DIR "FACES/INPUT/")
#_(def INPUT-GUEST-DIR (str INPUT-ROOT-DIR "GUESTS/"))
(def INPUT-ART-DIR (str INPUT-ROOT-DIR "ART/"))

(def OUTPUT-ROOT-DIR "FACES/OUTPUT/")
(def OUTPUT-ART-DIR (str OUTPUT-ROOT-DIR "ART"))

(def RE-ART (re-pattern
             (str INPUT-ART-DIR ".*\\.(gif|jpg|jpeg|tiff|png)$")))

(defn face-input-files [fileset]
  (inpt/input-files-by-re fileset [RE-ART]))

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
         "/" OUTPUT-ART-DIR
         "/" file-tag
         "_" rect-tag
         "_" name)))

(defn extract-faces [input-files outdir-path detector-config]
  (let [idx-input-files (indexed-input-files input-files detector-config)]
    (doseq [[img-idx input-img-name face-img idx-face-rects] idx-input-files]
      (doseq [[rect-idx rect] idx-face-rects]
        (let [result-path (output-path outdir-path
                                       (inc img-idx)
                                       (inc rect-idx)
                                       input-img-name)
              resized (img/resize-by-rect face-img rect)]
          (println "width: " (.-width rect))
          (println "result-path: " result-path)
          (println "make-parents: " (io/make-parents result-path))
          (println "file written: " (img/save-to-path resized result-path)))))))
