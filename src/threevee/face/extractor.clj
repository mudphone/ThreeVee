(ns threevee.face.extractor
  (:require
   [boot.core :as c :refer [deftask]]
   [threevee.face.detector :as detect]
   [threevee.input.core :as input]))

(def INPUT-ROOT-DIR "FACES/INPUT/")
#_(def INPUT-GUEST-DIR (str INPUT-ROOT-DIR "GUESTS/"))
(def INPUT-ART-DIR (str INPUT-ROOT-DIR "ART/"))

(def OUTPUT-ROOT-DIR "FACES/OUTPUT/")
(def OUTPUT-ART-DIR (str OUTPUT-ROOT-DIR "ART"))

(def RE-ART (re-pattern
             (str INPUT-ART-DIR ".*\\.(gif|jpg|jpeg|tiff|png)$")))

(defn face-input-files [fileset]
  (input/input-files-by-re fileset [RE-ART]))
