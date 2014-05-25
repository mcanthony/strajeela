;; (partial) port of Strasheela source/MusicUtils.oz

(ns ^{:doc "Utilities related to pitch processing including microtonal pitches."}
  strajeela.pitch
  ;; https://github.com/clojure/math.numeric-tower
  (:require [clojure.math.numeric-tower :as cl_math :refer [expt]]))


(defn- log2
  "Logarithm of x to base 2."
  [x]
  (/ (Math/log x) (Math/log 2)))


(def ^{:const true :private true}
  keynum-0-frequency
  "The frequency at the MIDI keynum 0 so that keynum 69 corresponds to 440 Hz."
  8.175798915643707)

(defn keynum->freq 
  "Transforms a keynum into the corresponding frequency in an equally tempered scale with keys-per-octave keys per octave. The function is 'tuned' such that (keynum->freq 69 12) returns 440.0 Hz. 

NB: The term keynum here is not limited to a MIDI keynumber, but denotes a keynumber in any equidistant tuning. For instance, if keys-per-octave=1200 then keynum denotes cent values."
  ([keynum] (keynum->freq keynum 12))
  ([keynum keys-per-octave]
     (* (Math/pow 2 (/ keynum keys-per-octave)) keynum-0-frequency)))

(defn freq->keynum 
  "Transforms freq into the corresponding keynum in an equally tempered scale with keys-per-octave keys per octave. The function is 'tuned' such that (freq->keynum 440 12) returns 69.0. 

NB: The term keynum here is not limited to a MIDI keynumber but denotes a keynumber in any equidistant tuning. For instance, if keys-per-octave=1200 then keynum denotes cent values."
  ([freq] (freq->keynum freq 12))
  ([freq keys-per-octave]
     (* (log2 (/ freq keynum-0-frequency)) keys-per-octave)))
  
(comment
  (keynum->freq 69) ; 440.0
  (keynum->freq 6900 1200) 

  (freq->keynum 440)
  (freq->keynum 440 12)
  )

(defn keynum->pc 
  "Transforms a keynumber (integer or float) in an equally tempered scale with keys-per-octave into its corresponding pitch class."
  ([keynum] (keynum->pc keynum 12))
  ([keynum keys-per-octave]
     (mod keynum keys-per-octave)))

(comment
  (keynum->pc 60)
  (keynum->pc 61.5)
  )

(defn ratio->standard-octave 
  "Expects a frequency ratio (ratio, float or integer) and transposes it by octaves into the interval [1/2, 2/2], i.e. effectively into a ratio pitch class."
  [freq-ratio]
  (cond 
   (>= freq-ratio 2) (ratio->standard-octave (/ freq-ratio 2))
   (<= freq-ratio 1) (ratio->standard-octave (* freq-ratio 2))
   :else freq-ratio))

(comment
  (ratio->standard-octave 3) ; 3/2
  (ratio->standard-octave 1/3) ; 4/3
  )


(defn ratio->keynum-interval 
  "Transforms ratio (either a float or a ratio) into the corresponding keynumber interval in an equally tempered scale with keys-per-octave keys per octave.
   
Examples:
(ratio->keynum-interval 1 12) => 0.0
(ratio->keynum-interval 3/2 12) => 7.01955

Note that a keynum here is not limited to a MIDI keynumber, but denotes a keynumber in any equidistant tuning. For instance, if keys-per-octave is 1200, keynum Keynum denotes cent values."
  [ratio keys-per-octave]
  (freq->keynum (* ratio keynum-0-frequency) keys-per-octave))


;; TODO: 
;;
;; - Translate a microtonal pitch notation (a string) -- either ASCCII form of Sagittal or of Extended Helmholtz-Ellis JI Pitch notation (EHE notation) into ratio
;;
;; - Round ratio to nearest tone of given scale (tuning table)
;;


(def ^:const HEWM-accidentals
  "Map mapping accidentals of HEWM notation to their respective ratios (see http://tonalsoft.com/enc/h/hewm.aspx ). Both, the accidentals of Joe Monzo and of Manuel op de Coul (in software Scala) are supported.

**Monzo**

lower raise  2,3,5,7,11-monzo       ratio      ~cents
  b    #    [-11  7,  0  0  0>    2187:2048  113.6850061
  v    ^    [ -5  1,  0  0  1>      33:32     53.2729432
  <    >    [  6 -2,  0 -1  0>      64:63     27.2640918
  -    +    [ -4  4, -1  0  0>      81:80     21.5062896

**de Coul**

lower raise  2,3,5,7,11-monzo      ratio      ~cents
  b    #    [-11  7  0  0  0]    2187:2048  113.6850061
  v    ^    [ -5  1  0  0  1]      33:32     53.2729432
  L    7    [  6 -2  0 -1  0]      64:63     27.2640918
  \\    /    [ -4  4 -1  0  0]      81:80     21.5062896
"
  {"bb" 4194304/4782969
   
   "b" 2048/2187
   "v" 32/33

   "<" 63/64 
   "7" 63/64 

   "-" 80/81
   "\\" 80/81

   "" 1

   "+" 81/80 
   "/" 81/80 

   ">" 64/63
   "L" 64/63

   "^" 33/32
   "#" 2187/2048

   "##" 4782969/4194304
   "x" 4782969/4194304})

(comment
  (HEWM-accidentals "#")
  (ratio->keynum-interval (HEWM-accidentals "x") 1200)
  )

(comment
  (def ^:const trojan-sagittal-accidentals
    {"bb" (prime-exponent-vector->ratio [22 -14 0 0 0 0 0 0 0])

     "b" (prime-exponent-vector->ratio [11 -7 0 0 0 0 0 0 0])
     "\\!!/" (prime-exponent-vector->ratio [11 -7 0 0 0 0 0 0 0])

     "b/|" (prime-exponent-vector->ratio [7 -3 -1 0 0 0 0 0 0])
     "!!/" (prime-exponent-vector->ratio [7 -3 -1 0 0 0 0 0 0])
     
     "b|)" (prime-exponent-vector->ratio [17 -9 0 -1 0 0 0 0 0])
     "!!)" (prime-exponent-vector->ratio [17 -9 0 -1 0 0 0 0 0])
     
     "\\!/" (prime-exponent-vector->ratio [5 -1 0 0 -1 0 0 0 0])

     "!)"

     "\\!"

     "|//|" (prime-exponent-vector->ratio [0 0 0 0 0 0 0 0 0])

     "/|"

     "|)"

     "/|\\"

     "||)"

     "#!)"
     "||)"

     "#\\!"
     "||\\"

     "#" (prime-exponent-vector->ratio [-11 7 0 0 0 0 0 0 0])
     "/||\\" (prime-exponent-vector->ratio [-11 7 0 0 0 0 0 0 0])

     "##" (prime-exponent-vector->ratio [-22 14 0 0 0 0 0 0 0])
     })
  )



(comment
  ;; TODO: unfinished
(defn sagittal->ratio 
  "Translates a sagittal pitch (symbol, keyword or string) into the corresponding ratio."
  [sagittal pitch]
  
  :test
  )
)

(def ^:const Pythagorean-nominals
  {"F" 4/3
   "C" 1/1
   "G" 3/2
   "D" 9/8
   "A" 27/16
   "E" 81/64
   "B" 243/128})


;; !! NOTE: This representation is not compatible with the **kern pitch representation, because in **kern the letter b is used for the pitch nominal, and the flat accidental is -. Besides, octaves are expressed without numbers. 
;; I will define parsing of the **kern pitch etc representation extra for importing **kern, and possibly I will also allow for selecting different pitch notations for inputting pitches to the Clojure score
;; TODO: Getting Combination of accidentals such as :Bb70 working. If I allow for accidental consisting of multiple characters later, then this is not trivial to add. With HEWM notation, I could simply split the accidentals into multiple characters, and interpret each character as its own accidents. With Sagittal I will likely not need any composite accidentals. So, I could have an extra optional argument specifying how to deal wit accidentals consisting of multiple characters. 
;; If I want to be even more flexible, then I could implement some proper parsing, but likely I never need that... 
(defn pc-symbol->ratio 
  "Translates a symbolic pitch class (symbol, keyword or string) into the corresponding ratio in standard octave [1/1, 2/1]. A pitch class consists of a nominal and an optional accidental. The nominals A, B, C and so on are Pythagorean fifths, with C representing 1/1. 

Besides the standard accidentals b, # and so on, also microtonal accidentals are supported. By default HEWM notation is supported (see http://tonalsoft.com/enc/h/hewm.aspx ), but different accidental can be defined and their meaning overwritten with a map mapping accidentals to ratios. 

Examples (with default accidental mapping)
:C => 1
:C# => 2187/2048

BUG: Combination of accidentals such as :Bb70 not yet supported"
  ([pc-symbol]
     (pc-symbol->ratio pc-symbol HEWM-accidentals))
  ([pc-symbol accidentals-map]
     (let [pc-string (name pc-symbol)
           nominal (str (first pc-string))
           accidental (apply str (rest pc-string))
           nominal-ratio (Pythagorean-nominals nominal)
           accidentals-ratio (accidentals-map accidental)]
       ;; (println nominal accidental nominal-ratio accidentals-ratio)
       (when-not nominal-ratio
         (throw (IllegalArgumentException.
                 (str nominal " is not a pitch nominal. Must be in set {A B C D E F G}."))))
       (when-not accidentals-ratio
         (throw (IllegalArgumentException.
                 (str accidental " is not an accidental. Must be a key in accidental map " accidentals-map))))
       (* nominal-ratio accidentals-ratio))))

(comment
  (pc-symbol->ratio "C#" HEWM-accidentals) ; 2187/2048
  (pc-symbol->ratio :G) ; 3/2
  (pc-symbol->ratio "B#") ; slightly above 2/1

  ;; errors
  (pc-symbol->ratio "X#" HEWM-accidentals)
  (pc-symbol->ratio "G?" HEWM-accidentals)
  )


(defn pitch-symbol->ratio 
  "Translates a symbolic pitch (symbol, keyword or string) into the corresponding ratio. A pitch consists of a nominal, an optional accidental, and an octave (an integer). See [pc-symbol->ratio] for further details on nominals and accidentals.

Examples (with default accidental mapping)
:C0 => 1
:C4 => 16
:C#4 => 2187/2048"
  ([pitch-symbol]
     (pitch-symbol->ratio pitch-symbol HEWM-accidentals))
  ([pitch-symbol accidentals-map]
     ;; (println pitch-symbol accidentals-map (name pitch-symbol))
     (let [pitch-string (name pitch-symbol)
           pc (apply str (butlast pitch-string))
           octave (Integer/parseInt (str (last pitch-string)))]
       ;; (println pc (pc-symbol->ratio pc) octave)
       (* (pc-symbol->ratio pc) (cl_math/expt 2 octave)))))

(comment
  (pitch-symbol->ratio :C0) ; 1
  (pitch-symbol->ratio :C4) ; 16
  (pitch-symbol->ratio :C#4) ; 2187/128
  (pitch-symbol->ratio :E-0) ; 5/4
  (pitch-symbol->ratio "E\\0") ; 5/4

  ;; BUG: 
  (pitch-symbol->ratio :Bb70)
  )


(defn prime-exponent-vector->ratio 
  "Expects a prime exponent vector and returns the corresponding ratio. This is useful, for example, for expressing frequency ratios broken down into individual prime-number based intervals; see, e.g., http://tonalsoft.com/enc/m/monzo.aspx ."
  [prime-exponents]
  (let [primes [2 3 5 7 11 13 17 19 23]]
    (reduce *
     (map (fn [prime exponent]
            (cl_math/expt prime exponent))
         primes
         prime-exponents))))

(comment
  (prime-exponent-vector->ratio [0 0 0 0 0 0 0 0 0])  ; 1
  (prime-exponent-vector->ratio [0]) ; 1
  (prime-exponent-vector->ratio [-1 1 0 0 0 0 0 0 0]) ; 3/2
  (prime-exponent-vector->ratio [-2 0 1 0 0 0 0 0 0]) ; 5/4

  (ratio->keynum-interval (prime-exponent-vector->ratio [22 -14 0 0 0 0 0 0 0]) 1200) ; ~ -227.37 corresponds to accidental bb
  )

(defn odd-limit 
  "Returns the odd limit of freq-ratio. See http://en.wikipedia.org/wiki/Odd_limit#Odd_limit"
  [freq-ratio]
  (let [denom (denominator freq-ratio)
        num (numerator freq-ratio)]
    (first (sort > (filter odd? [num denom])))))

(comment
  (odd-limit 3/2)  ; 3 
  (odd-limit 10/7) ; 7  
  (odd-limit 81/64) ; 81
  )


;; code from Óscar López and mikera, http://stackoverflow.com/questions/9556393/clojure-tail-recursion-with-prime-factors
(defn- primefactors 
  ([n] 
     (primefactors n 2 '()))
  ([n candidate acc]
     (cond (<= n 1) (reverse acc)
           (zero? (rem n candidate)) (recur (/ n candidate) candidate (cons candidate acc))
           :else (recur n (inc candidate) acc))))

(comment
  (primefactors 10) ; (2 5)
  (primefactors 70) ; (2 5 7)
  )

(defn prime-limit 
  "Returns the prime limit of freq-ratio. See http://en.wikipedia.org/wiki/Odd_limit#Prime_limit"
  [freq-ratio]
  (let [denom (denominator freq-ratio)
        num (numerator freq-ratio)]
    (first (sort > [(last (primefactors denom)) (last (primefactors num))]))))

(comment
  (prime-limit 3/2)  ; 3 
  (prime-limit 10/7) ; 7  
  (prime-limit 81/64) ; 3
  )

(defn is-et? 
  "Returns true if pitch-unit is a symbol/keyword/string which matches the pattern <Digit>+et such as :31et or '72et."
  [pitch-unit]
  (re-matches #"\d+et" (name pitch-unit)))

(comment
  ;; true
  (is-et? "12et")
  (is-et? :31et)
  ;; false
  (is-et? :xet)
  (is-et? :et31)
  (is-et? "false")
  (is-et? "ok")
  )


(defn get-pitches-per-octave 
  "Returns the pitches per octave expressed by an ET pitch unit, e.g., for :31et it returns 31."
  [et-pitch-unit]
  (Integer/parseInt (apply str (drop-last 2 (name et-pitch-unit)))))

(comment
  ;; returns int
  (get-pitches-per-octave "72et")
  (get-pitches-per-octave :31et)
  ;; error
  (get-pitches-per-octave 42)
  (get-pitches-per-octave 'test)
  )


(def ^:dynamic *tuning-table* 
  "This global tuning table is used when translating pitches into their corresponding midi floats for output. 

The format of a tuning table declaration is somewhat similar to the Scala scale file format (cf. http://www.huygens-fokker.org/scala/scl_format.html ). A tuning table is a vector of pitch specs, which are either floats (measured in cent) or ratios (including integers). The first degree is implicit (always 1/1 or 0.0). The highest pitch (i.e. the last value in a vector) is the period interval. 

Here is an example that defines 1/4-comma Meantone:
[76.04900 193.15686 310.26471 5/4 503.42157 579.47057 696.57843 25/16 889.73529 1006.84314 1082.89214 2/1] 

An unset tuning table (nil) corresponds to the equal tempered scale."
  nil)

(defn- full-tuning-table 
  "Expects a tuning table declaration (see [[*tuning-table*]] for format) and returns a full tuning table used for pitch computation. The returned full table is a map containing the actual table (a vector), the size and the period. The actual table contains all pitches measured in cent, and has 0.0 added as first table value."
  [tuning-table]
  (let [full-table (map (fn [p]
                                (if (or (ratio? p) (integer? p))
                                  (ratio->keynum-interval p 1200)
                                  p))
                           tuning-table)]
    {:table (apply vector (cons 0.0 full-table))
     :size (count full-table)
     :period (last full-table)}))

(comment
  ;; example tuning table: 1/4-comma Meantone 
  (def meantone [76.04900 193.15686 310.26471 5/4 503.42157 579.47057 696.57843 25/16 889.73529 1006.84314 1082.89214 2/1])
  (def full-meantone (full-tuning-table meantone))
  (:period full-meantone)
  (:table full-meantone)
  (get (:table full-meantone) 1)
  )



(defn pitch->midi 
  "Transforms pitch measured in pitch-unit (a symbol, keyword or string) into the corresponding 'Midi float', i.e. a Midi number where positions after the decimal point express microtonal pitch deviations (e.g., 60.5 is middle C raised by a quarter tone). Possible pitch units are midi (i.e., 12et), midicent/midic, frequency/freq/hz, and arbitrary equal temperaments (e.g., 31et, 72et).

The transformation takes into account a tuning table defined with *tuning-table*. Alternatively, a tuning table can be given directly to the optional arg tuning-table.

BUG: Currently, tuning tables are only supported for the pitch-unit midi."
  ([pitch pitch-unit] (pitch->midi pitch pitch-unit *tuning-table*))
  ([pitch pitch-unit tuning-table]     
     {:pre [(number? pitch)]}
     (if tuning-table
       ;; TMP: Tuning tables restriction 
       (if (not= (name pitch-unit) "midi")
         (throw (IllegalArgumentException.
                 "Tuning tables currently only supported for pitch-unit midi"))
         (let [full-table (full-tuning-table tuning-table)
               pc (mod pitch (:size full-table))
               octave (quot pitch (:size full-table))]
           (println :full-table full-table :pc pc :octave octave) 
           ;; TODO: add exception if tuning table size mismatches (see corresponding Strasheela code)
           (/ (+ (* (:period full-table) octave) (get (:table full-table) pc)) 100)))
       ;; without using any tuning table
       (case (name pitch-unit)
         "midi" pitch
         ("midicent" "midic") (/ pitch 100)
         "millimidicent" (/ pitch 100000)
         ("frequency" "freq" "hz") (freq->keynum pitch)
         (if (is-et? pitch-unit)
           (/ (* pitch 12) (get-pitches-per-octave pitch-unit))
           (throw (IllegalArgumentException. 
                   (str pitch-unit 
                        " is not a pitch unit. Supported pitch units are the midi, midicent (or midic), frequency (or freq, hz), and arbitrary equal temperaments (notated <Digit>+et)."))))))))

(comment
  ;; ok
  (pitch->midi 60 :midi) 
  (pitch->midi 440 :hz) ; 69.0
  (pitch->midi (* 8.175798915643707 3/2 32) :hz) ; about 67.019
  (pitch->midi (* 5 31) :31et) ; 60

  ;; errors
  (pitch->midi 60 :bla)
  (pitch->midi 'test :midi)

  ;; using tuning tables
  (def et12 "Dummy table for testing"
    [100.0 200.0 300.0 400.0 500.0 600.0 700.0 800.0 900.0 1000.0 1100.0 1200.0])
  (def meantone [76.04900 193.15686 310.26471 5/4 503.42157 579.47057 696.57843 25/16 889.73529 1006.84314 1082.89214 2/1])
  (pitch->midi 60 :midi et12)
  (pitch->midi 61 :midi et12)
  (pitch->midi 61 :midi meantone)
  (pitch->midi 73 :midi et12)
  ;; BUG: desired results shown, but not achieved 
  (pitch->midi 440 :hz et12) ; 69.0
  (pitch->midi (* 5 31) :31et et12) ; 60
  )

