;;  Author: Luke VanderHart
;;  Creation Date: 4/16/2009

(ns tubes.components.sort
  (:use tubes.engine))

(def-component {:name "sort-records"
                :category "Transform"
                :description "Sorts a record sequence"
                :output-type "Record Sequence"
                :args {"key" {:doc "The record key to use for the sort"
                              :type "String"
                              :min-required 1
                              :max-required 1}
                       "input" {:doc "The record sequence to sort"
                                :type "Record Sequence"
                                :min-required 1
                                :max-required 1}}}
  (fn [pipe pipe-args args]
    (sort-by #(% (args "key") )(args "input") )))