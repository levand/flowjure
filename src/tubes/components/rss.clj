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
            (assoc acc (:tag it) (first (:content it)))) {} (:content item)))

(def-component {:name "rss"
                :type "Input"
                :description "Loads an RSS Feed"
                :min-inputs 0
                :max-inputs 0
                :args {:url "The URL of the RSS feed"}}
  (fn [pipe pipe-args inputs cfg]
    (let [xml (xml/parse (:url cfg))
          zipper (zip/xml-zip xml)
          elements (-> zipper zip/down zip/children)
          items (filter #(= :item (:tag %)) elements)]
      (map create-map items))))