(ns threevee.face.core
  (:require
   [boot.core :as c :refer [deftask]]
   [threevee.face.detector :as detect]
   [threevee.face.extractor :as extract]))

(deftask extract-faces []
  (let [tmp (c/tmp-dir!)]
    (println "extracting faces...")
    (fn middleware [next-handler]
      (fn handler [fileset]
        (c/empty-dir! tmp)
        (let [input-files (extract/face-input-files fileset)
              detector (detect/haar-face-detector fileset)
              detector-config (detect/detector-config detector)]
          (extract/extract-faces input-files
                                 (.getPath tmp)
                                 detector-config))
        (-> fileset
            (c/add-asset tmp)
            c/commit!
            next-handler)))))
