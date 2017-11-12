# ThreeVee
OpenCV 3 with Clojure &amp; Quil

A Quil sketch designed to detect faces with OpenCV 3.3 and LISP!

## Usage

LightTable - open `core.clj` and press `Ctrl+Shift+Enter` to evaluate the file.

Emacs - run cider, open `core.clj` and press `C-c C-k` to evaluate the file.

REPL - run `(require 'threevee.core)`.

## Notes

* [find the OpenCV 3.3 jar files with Homebrew](http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html)
* [3.3 face detection demo code](http://www.rmnd.net/install-and-use-opencv-3-0-on-mac-os-x-with-eclipse-java/)
* [original 2.4 face detection demo](https://blog.openshift.com/day-12-opencv-face-detection-for-java-developers/)
* [building OpenCV jars (if you don't get them from Homebrew)](https://docs.opencv.org/3.0-beta/doc/tutorials/introduction/desktop_java/java_dev_intro.html)
* [official Clojure OpenCV 2.4 tutorial](https://docs.opencv.org/2.4/doc/tutorials/introduction/clojure_dev_intro/clojure_dev_intro.html)
* [official Clojure OpenCV 3.3 tutorial](https://docs.opencv.org/trunk/d7/d1e/tutorial_clojure_dev_intro.html)
* [OpenCV + Java face detection & tracking](http://opencv-java-tutorials.readthedocs.io/en/latest/06-face-detection-and-tracking.html)
* [official OpenCV 2.4 Java development tutorial](https://docs.opencv.org/2.4/doc/tutorials/introduction/desktop_java/java_dev_intro.html)
* [my old work with Clojure and OpenCV 2.4](https://github.com/PasDeChocolat/QuilCV)
* [wekinator face tracking, with OpenCV 2.4](http://www.wekinator.org/walkthrough/)

### OpenCV Required

You'll have to build and install the OpenCV JAR files for your setup. Installation tested on Archlinux and OS X. The following is a rough summary of "[Introduction to OpenCV Development with Clojure](http://docs.opencv.org/doc/tutorials/introduction/clojure_dev_intro/clojure_dev_intro.html#clojure-dev-intro)."

Note, at time of writing, the latest version was 3.3.0. So, you may need to replace `330` in the instructions below with the current version.

#### Java
Confirm Java is already installed:
```` bash
$ java -version
java version "1.8.0_144"
Java(TM) SE Runtime Environment (build 1.8.0_144-b01)
Java HotSpot(TM) 64-Bit Server VM (build 25.144-b01, mixed mode)
````

Otherwise, install it.

#### On OS X, I used Homebrew, requiring the following

```` bash
brew update
brew install cmake
brew install python2
brew install ant
````

If python2 not available, make sure you have Python pointing to Python v2.

#### Build the OpenCV JAR files

> To install OpenCV (with Java support) through Homebrew, you need to edit the opencv formula in Homebrew, to add support for Java: brew edit opencv In the text editor that will open, change the line: -DBUILD_opencv_java=OFF in -DBUILD_opencv_java=ON then, after saving the file, you can effectively install OpenCV: brew install --build-from-source opencv

> After the installation of OpenCV, the needed jar file and the dylib library will be located at /usr/local/Cellar/opencv/3.x.x/share/OpenCV/java/, e.g., /usr/local/Cellar/opencv/3.3.0_3/share/OpenCV/java/.

NO NEED TO INSTALL IT, WE JUST NEED THE JAR. So, there's no reason to install it.

#### Use lein-localrepo to install JAR files

Create a file named profiles.clj in the ~/.lein directory and copy into it the following content:
```` clojure
{:user {:plugins [[lein-localrepo "0.5.4"]]}}
````

You may need to change `0.5.4` to the latest version of `lein-localrepo`.

```` bash
mkdir clj-opencv
cd clj-opencv
cp <path>/<to>/opencv/build/bin/opencv-330.jar .
mkdir -p native/macosx/x86_64
cp ../opencv/build/lib/libopencv_java330.dylib native/macosx/x86_64
jar -cMf opencv-native-330.jar native
lein localrepo install opencv-330.jar opencv/opencv 3.3.0
lein localrepo install opencv-native-330.jar opencv/opencv-native 3.3.0
````

## Run with Boot

Leiningen has an `injections` feature (in `project.clj`) which loads the native OpenCV libraries. This doesn't work in Boot, because the `java.library.path` is not the same. Set this up manually from the command line (doesn't work in the `boot.properties` file).

```` fish
env BOOT_JVM_OPTIONS="$BOOT_JVM_OPTIONS -Djava.library.path=/path/to/native/macosx/x86_64" boot faces
````

For `/path/to/native`, see location of exploded `opencv-native-xxx.jar` above.

## License

Copyright © 2017 Pas de Chocolat, LLC

Distributed under the MIT License
