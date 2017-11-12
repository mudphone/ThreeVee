(set-env!
  :asset-paths #{"assets"}
  :source-paths #{"src"}
  :dependencies '[[opencv/opencv-native "3.3.0"]
                  [opencv/opencv "3.3.0"]])

;; Unfortunately, launch via Boot requires:
;; $ env BOOT_JVM_OPTIONS="$BOOT_JVM_OPTIONS -Djava.library.path=/path/to/native/macosx/x86_64" boot repl
;; see: https://github.com/boot-clj/boot/issues/185
;; e.g.
;; $ env BOOT_JVM_OPTIONS="$BOOT_JVM_OPTIONS -Djava.library.path=/Users/koba/work/PDC/COMPUTER_VISION/clj-opencv3/native/macosx/x86_64" boot repl
;;
(clojure.lang.RT/loadLibrary org.opencv.core.Core/NATIVE_LIBRARY_NAME)
(require '[threevee.face.core :refer [extract-faces]])

(deftask faces
  "Extract faces"
  []
  (comp (extract-faces)
        (target)
        ))
