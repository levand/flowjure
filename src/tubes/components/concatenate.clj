;;  Author: Luke VanderHart
;;  Creation Date: 4/16/2009

(ns tubes.components.concatenate
  (:use tubes.engine))

(def-component {:name "concatenate"
                :category "Transform"
                :description "Concats two record sequences"
                :output-type "Record Sequence"
                :args {"inputs" {:doc "The record sequences to concatnate"
                                :type "Record Sequence"
                                :min-required 1}}}
  (fn [pipe pipe-args args]
    ((fn cat-inputs [inputs]
       (lazy-seq
         (if (seq inputs)
           (concat (first inputs) (cat-inputs (rest inputs)))
           '())))
      (args "inputs"))))