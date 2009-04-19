;;  Author: Luke VanderHart
;;  Creation Date: 4/16/2009

(ns tubes.components.simple-out
  (:require [clojure.contrib.pprint :as pprint])
  (:use tubes.engine))

(def-component {:name "simple-out"
                :type "Output"
                :description "Outputs a pipe as a Clojure string"
                :min-inputs 1
                :max-inputs 1
                :args {}}
  (fn [pipe pipe-args inputs args]
    (str (vec (first inputs)))))

   