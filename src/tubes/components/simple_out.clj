;;  Author: Luke VanderHart
;;  Creation Date: 4/16/2009

(ns tubes.components.simple-out
  (:require [clojure.contrib.pprint :as pprint])
  (:use tubes.engine))

(def-component {:name "simple-out"
                :category "Output"
                :description "Outputs a pipe as a Clojure string"
                :output-type "String"
                :args {"input" {:doc "The input"
                                :type "Record Sequence"
                                :min-required 1
                                :max-required 1}}}
  (fn [pipe pipe-args args]
      (str (vec (args "input")))))

   