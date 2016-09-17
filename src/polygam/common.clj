(ns polygam.common
  (:require [clojure.core.logic :only    [== >= <= > < = !=] :as l])
  (:use     [clojure.core.logic :exclude [== >= <= > < = !=]]
            [clojure.core.logic.pldb]
            [polygam.facts])
  (:require [clojure.core.logic.arithmetic :as ar]
            [clojure.core.logic.fd :as fd]))

(defn- order-relationo
  "Abstract the general pattern of a strict order relation in a logic,
  relational way."
  [relation x y]
  (conde
   [(relation x y)]
   [(fresh [z]
      (relation x z)
      (order-relationo relation z y))]))

(def kino
  "A goal where the two inputs x and y share kinship: x is an ancestor of y and
  y a descandant of x."
  (partial order-relationo child))

(defn siblingso
  [x s]
  (fresh [z]
    (child z x)
    (child z s)
    (l/!= x s)))

(defn empeachedo
  [q]
  (all
   (fresh [z]
     (vertex z)
     (yap z)
     (siblingso q z))
   (nafc yap q)))

(defn single-matcho
  "Reduce multiple matches to a single one but get the computation much slower"
  [goal]
  (nafc #(nafc goal)))

