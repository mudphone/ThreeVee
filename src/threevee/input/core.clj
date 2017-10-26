(ns threevee.input.core)

(def INPUT-DIR  "INPUT")
(def OUTPUT-DIR "OUTPUT")

(defn input-files [dir-name]
  (file-seq
   (clojure.java.io/file dir-name)))

(def map-file-name-xr
  (map (fn [file]
         [(.getName file) (.toString file)])))

(defn input-file-names []
  (into [] map-file-name-xr (input-files INPUT-DIR)))

(def filter-pic-name-xr
  (filter (fn [[file-name _]]
            (re-find
             (re-pattern ".*\\.(gif|jpg|jpeg|tiff|png)$")
             file-name))))

(defn input-pic-names []
  (let [xr (comp
            map-file-name-xr
            filter-pic-name-xr)]
    (into [] xr (input-files INPUT-DIR))))

(defn output-pic-name
  ([input-pic-name]
   (output-pic-name input-pic-name nil))
  ([input-pic-name extra]
   (let [tag (if-not (nil? extra)
               (str extra "_")
               "")]
     (str OUTPUT-DIR "/output_" tag input-pic-name))))

