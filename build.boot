(set-env!
  :asset-paths #{"assets"}
  :source-paths #{"src"}
  :dependencies '[[opencv/opencv-native "3.3.0"]
                  [opencv/opencv "3.3.0"]
                  [org.clojure/math.numeric-tower "0.0.4"
                   :exclusions [org.clojure/clojure]]
                  [org.clojure/tools.namespace "0.2.11"
                   :exclusions [org.clojure/clojure]]])

;; Reloading CLJ files from the Boot REPL:
;; https://github.com/boot-clj/boot/wiki/Repl-reloading
;; Refresh updated clj files with:
(require '[clojure.tools.namespace.repl :as repl])
(apply repl/set-refresh-dirs (get-env :directories))
(comment
  (ns-unalias *ns* 'face-tasks)
  (repl/refresh))

;; Unfortunately, launch via Boot requires:
;; $ env BOOT_JVM_OPTIONS="$BOOT_JVM_OPTIONS -Djava.library.path=/path/to/native/macosx/x86_64" boot repl
;; see: https://github.com/boot-clj/boot/issues/185
;; e.g.
;; $ env BOOT_JVM_OPTIONS="$BOOT_JVM_OPTIONS -Djava.library.path=/Users/koba/work/PDC/COMPUTER_VISION/clj-opencv3/native/macosx/x86_64" boot repl
;;
(clojure.lang.RT/loadLibrary org.opencv.core.Core/NATIVE_LIBRARY_NAME)
(require '[threevee.face.core :as face-tasks])

(deftask extract-art-faces
  "Extract faces from artwork photos"
  []
  (comp (face-tasks/extract-art-faces)
        (target)))

(deftask extract-guest-faces
  "Extract faces from guest photos"
  []
  (comp (face-tasks/extract-guest-faces)
        (target)))

(deftask mark-art-faces
  "Draw a bounding rectangle around detected faces"
  []
  (comp (face-tasks/mark-art-faces)
        (target)))

(deftask preprocess-guest-faces
  "Normalize guest faces"
  []
  (comp (face-tasks/preprocess-guest-faces)
        (target)))

(deftask all
  "Do all"
  []
  (comp (mark-art-faces)
        (extract-art-faces)))
