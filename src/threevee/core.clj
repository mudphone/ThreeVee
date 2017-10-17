(ns threevee.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn detect-face []
  (let [face-detector (org.opencv.objdetect.CascadeClassifier. "haarcascade_frontalface_alt.xml")
        image (org.opencv.imgcodecs.Imgcodecs/imread "obama.jpg")
        face-detections (org.opencv.core.MatOfRect.)
        _ (.detectMultiScale face-detector image face-detections)
        img-rect (first (.toArray face-detections))
        a (org.opencv.core.Point. (.-x img-rect) (.-y img-rect))
        b (org.opencv.core.Point. (+ (.-x img-rect) (.-width img-rect)) (+ (.-y img-rect) (.-height img-rect)))]
    (.detectMultiScale face-detector image face-detections) (org.opencv.imgproc.Imgproc/rectangle image a b (org.opencv.core.Scalar. 0 255 0))
    (org.opencv.imgcodecs.Imgcodecs/imwrite "output.png" image)))

(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  (detect-face)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {})

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  state)

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ; Set circle color.
  (q/fill 135 255 255)
  ; Calculate x and y coordinates of the circle.
  (let [angle 0
        x (* 150 (q/cos angle))
        y (* 150 (q/sin angle))]
    ; Move origin point to the center of the sketch.
    (q/with-translation [(/ (q/width) 2)
                         (/ (q/height) 2)]
      ; Draw the circle.
      (q/ellipse x y 100 100))))

(q/defsketch threevee
  :title "You spin my circle right round"
  :size [500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
