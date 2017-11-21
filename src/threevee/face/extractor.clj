(ns threevee.face.extractor
  (:require
   [boot.core :as c]
   [threevee.face.detector :as detect]
   [threevee.image.core :as img]))

(defn- output-path [root out-path img-idx rect-idx name]
  (let [file-tag (format "%04d" img-idx)
        rect-tag (format "%02d" rect-idx)]
    (str root
         "/" out-path
         "/" file-tag
         "_" rect-tag
         "__" name)))

(defn crop-face [[rect-idx rect] tmp-path out-path img-idx input-img-name face-img]
  (let [result-path (output-path tmp-path
                                 out-path
                                 (inc img-idx)
                                 (inc rect-idx)
                                 input-img-name)
        resized (img/resize-by-rect face-img rect)]
    (println "width: " (.-width rect))
    (println "result-path: " result-path)
    (println "file written: " (img/save-to-path resized result-path))
    {:face-image resized
     :path result-path}))

(defn extract-faces-from-image [tmp-path out-path img-idx input-img-name face-img idx-face-rects]
  (for [rect idx-face-rects]
    (crop-face rect tmp-path out-path img-idx input-img-name face-img)))

(defn grab-faces-from-image [images-info tmp-path out-path]
  (->> images-info
       (map (fn [{:keys [image-index image-name indexed-face-rects]
                   image :processed-image :as info}]
               (let [ext-faces (for [rect indexed-face-rects]
                                 (crop-face rect tmp-path out-path image-index image-name image))]
                 (println (str image-index ": extracted") (count ext-faces) "faces")
                 (merge info {:extracted-faces ext-faces}))))))

(defn extract-faces [input-files tmp-path out-path detector-config]
  (let [files (map c/tmp-file input-files)
        idx-input-files (detect/indexed-preprocessed-input-images files detector-config)
        ;;idx-input-files (detect/indexed-input-images files detector-config)
        ]
    (count
     (-> idx-input-files
         (grab-faces-from-image tmp-path out-path)))))
