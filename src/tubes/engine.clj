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

(defmacro reference
  "Takes a normal string intended as a reference, and concatnates a tag to it
to ensure that it is tracked as a reference string and not a literal value"
  [string]
  (str "COMPONENT-REF:" string))

(defn resolve-ref
  "Resolves a reference which may be a simple string, or a component ref."
  [pipe ref-string]
  (if (. ref-string startsWith "COMPONENT-REF:")
    (let [ref-name (. ref-string substring (count "COMPONENT-REF:"))
          component-function (pipe ref-name)]
      (apply component-function))
    ref-string))

(defn resolve-args
  "Takes an args map from a component instance definition and, at runtime,
resolves references and literal values"
  [args pipe]
  (reduce conj {} (map (fn [entry]
                         [(key entry)
                          (if (string? (val entry))
                            (resolve-ref pipe (val entry))
                            (map (partial resolve-ref pipe) (val entry)))])
                    args)))

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
     [args#]
     (let [action-fun# ~fun
           pipe# tubes.engine/*pipe*
           pipe-args# tubes.engine/*pipe-args*]
       (dosync
         (let [pipe-key# (if (= (:category ~component-cfg) "Output")
                           "pipe-output"
                           (args# "id"))] ;; Special name for output
         (alter pipe# assoc pipe-key# (fn []
                                         (action-fun# pipe#
                                                      pipe-args#
                                                      (resolve-args args# pipe#)))))))))
