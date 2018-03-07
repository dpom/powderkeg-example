(ns powderkeg-example.scratch
  (:require
   [powderkeg.core :as keg]
   [clojure.math.combinatorics :as comb]
   [net.cgrand.xforms :as x])
  (:import
   [org.apache.spark.sql SparkSession SQLContext]
   [org.apache.spark.sql.types StringType StructField StructType]
   [org.apache.spark.sql.types DataTypes]
   [org.apache.spark.sql Row SaveMode RowFactory]
   ;; mllib
   [breeze.linalg DenseVector]
   [org.apache.spark.mllib.linalg Vectors]
   [org.apache.spark.mllib.regression StreamingLinearRegressionWithSGD LabeledPoint]
   [org.apache.spark.streaming Seconds StreamingContext]))


(keg/connect! "local[*]")

;; (keg/connect! "spark://84.40.60.42:10000")

(def sc keg/*sc*)

(def orders (.textFile sc "/home/dan/emag/rec-engine-api.clojure/data/order_prods_2y.csv"))
;; (def orders (.textFile sc "/home/dan/emag/rec-engine-api.clojure/data/order_prods.tsv"))


(into [] (keg/rdd orders
                          (map read-string)
                          (map #(comb/combinations % 2))
                          (map #(map (fn [x] (sort x))  %))
                          (take 5)
                          ))


(into {} (.take (keg/by-key (keg/rdd orders
                                     (map read-string)
                                     (map #(comb/combinations % 2))
                                     (map #(map (fn [x] (sort x))  %)))
                            :key count
                            (x/reduce merge))
                5))

(take (keg/into []  (reduce merge (keg/rdd orders
                                     (map read-string)
                                     (map #(comb/combinations % 2))
                                     (map #(map (fn [x] (sort x))  %)))))
                5)

(def d1 (keg/rdd orders
                 (map read-string)
                 (map #(comb/combinations % 2))
                 (map #(map (fn [x] (sort x))  %))
                 (take 5)
                 ))

(keg/into [] (mapcat identity  d1))


(into [] (-> orders
                 (keg/rdd
                      (map read-string)
                      (map #(comb/combinations % 2))
                      (map #(map (fn [x] (sort x))  %))
                      (mapcat identity)
                      )
                   (keg/by-key :key identity
                         :pre x/count
                         :post (x/reduce +))
                   (keg/rdd
                    (filter #(< 2 (second %)))
                    (take 5)))
      )




