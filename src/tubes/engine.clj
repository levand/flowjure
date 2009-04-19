;;  Author: Luke VanderHart
;;  Creation Date: 4/16/2009

(ns tubes.engine)

(def *pipe* nil)
(def *pipe-args* nil)

(defn load-pipe
  "Loads a pipe by parsing a pipe file. Returns a function which accepts the
pipe's arguments and returns the pipe's output value when called."
  [pipe-file]
  (binding [*pipe* (ref {})
            *pipe-args* (ref [])]
    (do
      (load-file pipe-file)
      (let [pipe *pipe*, pipe-args *pipe-args*]
        (fn [& args]
          (do
            (dosync (ref-set pipe-args args))
            ;; Find the output component
            (apply (@pipe "pipe-output"))))))))

(defmacro pipe
  "Macro that handles the 'pipe' form in a tube definition. Adds the config
object as metadata to the *pipe* object."
  [config]
  `(do
     (require ~@(map (fn [a#] (list 'quote (symbol a#))) (:require config)))
     (dosync
       (let [pipe# @*pipe*]
         (ref-set *pipe* (with-meta pipe# (merge ^pipe# ~config)))))))

(defmacro def-component
  "Expands to the full definition of a component. Resulting function
ought only to be called when defining a pipe, or else it will throw an NPE.
Takes a config object and a function. The function must accept
four arguments: the a ref to the pipe instance, a ref to the pipe arguments,
a seq of the inputs to the function (each a seq of maps), and a config object.
It must return a seq unless it is of type 'Output'"
  [component-cfg fun]
  `(defn ~(with-meta (symbol (:name component-cfg)) {:pipe-component component-cfg})
     ~(:description component-cfg)
     [cfg#]
     (let [action-fun# ~fun
           pipe# tubes.engine/*pipe*
           pipe-args# tubes.engine/*pipe-args*]
       (dosync
         (let [pipe-key# (if (= (:type ~component-cfg) "Output")
                          "pipe-output" (:id cfg#))] ;; Special name for output
         (alter pipe# assoc pipe-key# (fn []
                                         (action-fun# pipe# pipe-args#
                                           (map (fn [arg#] (apply (get @pipe# arg#))) (:input cfg#))
                                           cfg#))))))))
   
(comment
  ; This is a sample macro expansion of the following form:
  (def-component {:name "rss"
                  :type "Inputter"
                  :description "Loads an RSS Feed"
                  :min-inputs 0
                  :max-inputs 0
                  :args {:url "The URL of the RSS feed"}}
    (fn [pipe pipe-args inputs args]
      (println (:url args))))

  ;; The macro expansion:
 
  (defn #^{:pipe-component {:name "rss"
                            :type "Inputter"
                            :description "Loads an RSS Feed"
                            :min-inputs 0
                            :max-inputs 0
                            :args {:url "The URL of the RSS feed"}}}
    rss
    "Loads an RSS Feed"
    [cfg]
    (let [action-fun (fn [pipe pipe-args inputs args]
                       (println (:url args)))
          pipe tubes.engine/*pipe*
          pipe-args tubes.engine/*pipe-args*]
      (dosync
        (alter pipe assoc (:id cfg) (fn []
                                      (action-fun pipe pipe-args
                                        (map #(get @pipe %) (:input cfg))
                                        cfg))))))
  )


