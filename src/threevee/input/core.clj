(ns threevee.input.core
  (:require
   [boot.core :as c]))

(def INPUT-DIR  "INPUT")
(def OUTPUT-DIR "OUTPUT")


;; Operating on Java filesa

(defn file->name [jfile]
  [(.getName jfile) (.toString jfile)])


;; Operating on Boot fileset & "input-files" (tmpfiles)

(defn input-files-by-re [fileset res]
  (c/by-re res
           (c/input-files fileset)))

(defn tmpfile->name [tfile]
  (-> tfile c/tmp-file file->name))

(defn print-names [tfiles]
  (doseq [tfile tfiles]
    (let [[name _] (tmpfile->name tfile)]
      (println "input file:" name))))


;; Older fns using direct filesystem access.

(defn input-files [dir-name]
  (file-seq
   (clojure.java.io/file dir-name)))

(def map-file-name-xr
  (map file->name))

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

