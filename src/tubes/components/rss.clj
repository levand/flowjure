;;  Author: Luke VanderHart
;;  Creation Date: 4/16/2009

(ns tubes.components.rss
  (:require [clojure.xml :as xml]
             [clojure.zip :as zip])
  (:use tubes.engine))

(defn create-map
  "Creates a map object from an RSS 'item' entry"
  [item]
  (reduce (fn [acc it]
            (assoc acc (keyword-to-str (:tag it)) 
              (first (:content it)))) {} (:content item)))

(def-component {:name "rss"
                :category "Input"
                :description "Loads an RSS Feed"
                :output-type "Record Sequence"
                :args {"url" {:doc "The URL of the RSS feed"
                              :type "String"
                              :min-required 1
                              :max-required 1}}}
  (fn [pipe pipe-args args]
    (let [xml (xml/parse (args "url"))
          zipper (zip/xml-zip xml)
          elements (-> zipper zip/down zip/children)
          items (filter #(= :item (:tag %)) elements)]
      (map create-map items))))