;;; tg-mode.el --- Major mode for editing TG files with emacs

;; Copyright (C) 2007, 2008, 2009 by Tassilo Horn

;; Author: Tassilo Horn <horn@uni-koblenz.de>

;; This program is free software; you can redistribute it and/or modify it
;; under the terms of the GNU General Public License as published by the Free
;; Software Foundation; either version 3, or (at your option) any later
;; version.

;; This program is distributed in the hope that it will be useful, but WITHOUT
;; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
;; FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
;; more details.

;; You should have received a copy of the GNU General Public License along with
;; this program ; see the file COPYING.  If not, write to the Free Software
;; Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

;;; Commentary:

;; Major mode for editing TG files with Emacs.  Include superior navigation
;; functions, a full schema parser, and eldoc capabilities.

;;; Version
;; $Revision$

;;; Todos:
;; - Show from/to when eldocing EdgeClasses.

;;* Code

(when (not (fboundp 'defparameter))
  (defmacro defparameter (symbol &optional initvalue docstring)
    "Common Lisps defparameter."
    `(progn
       (defvar ,symbol nil ,docstring)
       (setq   ,symbol ,initvalue))))

;;** Schema parsing

(defvar tg-schema-alist nil
  "The schema of the current TG file.")
(make-variable-buffer-local 'tg-schema-alist)

(defun tg-init-schema ()
  (setq tg-schema-alist (tg--parse-schema))
  (tg--init-unique-name-hashmap))

(defun tg--parse-schema ()
  "Parse the schema of the current schema/graph file."
  (save-excursion
    (goto-char (point-min))
    (let ((current-package "")
          schema-alist
          finished)
      (while (not finished)
        (cond
         ;; Packages
         ((looking-at "^Package\s+\\([[:alnum:]._]+\\)")
          (let ((match (match-string-no-properties 1)))
            (setq current-package (if (string-match "^\s*$" match)
                                      ""
                                    (concat  (match-string-no-properties 1) ".")))))
         ;; GraphClass
         ((looking-at "^GraphClass\s+\\([[:alnum:]._]+\\)\s*\\(?:{\\([^}]*\\)}\\)?"))
         ;; VertexClass
         ((looking-at (concat "^\\(?:abstract\s+\\)?"
                              "VertexClass\s+"
                              "\\([[:alnum:]._]+\\)\s*"
                              "\\(?::\\([^{[;]+\\)\\)?\s*" ;; Superclasses
                              "\\(?:{\\([^}]*\\)}\\)?"     ;; Attributes
                              "\\(?:[[].*[]]\\)*\s*;"      ;; Constraints
                              ))
          (setq schema-alist
                (cons (list 'VertexClass
                            (concat current-package (match-string-no-properties 1))
                            (tg--parse-superclasses (match-string-no-properties 2) current-package)
                            (tg--parse-attributes (match-string-no-properties 3)))
                      schema-alist)))
         ;; EdgeClasses
         ((looking-at (concat "^\\(?:abstract\s+\\)?"
                              "\\(?:Edge\\|Aggregation\\|Composition\\)Class\s+"
                              "\\([[:alnum:]._]+\\)\s*" ;; Name
                              "\\(?::\\([[:alnum:]._ ]+\\)\\)?\s*" ;; Supertypes
                              "\\<from\\>\s+\\([[:alnum:]._]+\\)\s+.*\\<to\\>\s+\\([[:alnum:]._]+\\)\s+.*" ;; from/to
                              "\\(?:{\\([^}]*\\)}\\)?" ;; Attributes
                              "\\(?:[[].*[]]\\)*\s*;"  ;; Constraints
                              ))
          (let ((from (match-string-no-properties 3))
                (to   (match-string-no-properties 4)))
            (save-match-data
              (setq from (if (string-match "\\." from) from (concat current-package from)))
              (setq to   (if (string-match "\\." to)   to   (concat current-package to))))
            (setq schema-alist
                  (cons (list 'EdgeClass
                              (concat current-package (match-string-no-properties 1))
                              (tg--parse-superclasses (match-string-no-properties 2) current-package)
                              (tg--parse-attributes (match-string-no-properties 5))
                              from
                              to)
                        schema-alist))))
         ;; End of schema (part)
         ((or (= (point) (point-max))
              (looking-at "Graph[[:space:]]+"))
          (setq finished t)))
        (forward-line))
      schema-alist)))

(defun tg--parse-superclasses (str current-package)
  "Given a string \"Foo, Bar, Baz\" it returns (\"Foo\" \"Bar\"
\"Baz\") where Foo, Bar, Baz are fully qualified."
  (when str
    (setq str (replace-regexp-in-string "[[:space:]]+" "" str))
    (save-match-data
      (mapcar
       (lambda (class) (if (string-match "\\." class)
                           ;; It's already qualified
                           class
                         ;; Not qualified ==> add the package prefix
                         (concat current-package class)))
       (split-string str "[,]+")))))

(defun tg--parse-attributes (str)
  (when str
    (save-match-data
      (setq str (replace-regexp-in-string "[[:space:]]+" "" str))
      (let ((list (split-string str "[:,]+"))
            result
            (i 1))
        (dolist (elem list)
          (when (= (mod i 2) 1)
            (setq result (cons elem result)))
          (setq i (+ i 1)))
        result))))

(define-hash-table-test 'string= 'string= 'sxhash)
(defvar tg-unique-name-hashmap nil
  "Maps qualified names to unique names and vice versa.")
(make-variable-buffer-local 'tg-unique-name-hashmap)

(defun tg--init-unique-name-hashmap ()
  (setq tg-unique-name-hashmap
        (if tg-unique-name-hashmap 
            (clrhash tg-unique-name-hashmap)
          (make-hash-table :test 'string=)))
  (dolist (l tg-schema-alist)
    (let* ((qname (second l))
           (uname (replace-regexp-in-string "\\(?:.*\\.\\)\\([^.]+\\)" "\\1" qname)))
      (if (not (tg--unique-name-p uname))
          ;; Not unique
          (puthash qname qname tg-unique-name-hashmap)
        (puthash uname qname tg-unique-name-hashmap)
        (puthash qname uname tg-unique-name-hashmap)))))

(defun tg--unique-name-p (name)
  (catch 'tg-unique
    ;; when name is already in it, there's no change of being unique.
    (when (gethash name tg-unique-name-hashmap)
      (throw 'tg-unique nil))
    (dolist (line tg-schema-alist)
      (when (or (eq (car line) 'VertexClass)
                (eq (car line) 'EdgeClass))
        (let ((simple-name (replace-regexp-in-string
                            "\\(?:.*\\.\\)\\([^.]+\\)" ""
                            (second line))))
          (when (string= name simple-name)
            (throw 'tg-unique nil)))))
    (throw 'tg-unique t)))

;;** Schema querying

(defun tg-attributes-multi (mtype types)
  "Returns a list of all attribute names that are defined in all
MTYPEs TYPES."
  (let ((attr-list (mapcar
                    (lambda (type)
                      (tg-all-attributes mtype type))
                    types)))
    (if (= (length attr-list) 1)
        (car attr-list)
      (apply 'intersection
             attr-list))))

(defun tg-all-attributes (mtype type &optional with-supertype)
  "Returns a list of all attribute names of the MTYPE TYPE (and
its supertypes)."
  (let ((line (tg-find-schema-line mtype type)))
    (sort
     (remove-duplicates
      (apply 'append
             (fourth line)
             (mapcar (lambda (supertype)
                       (let ((attrs (tg-all-attributes mtype supertype with-supertype)))
                         (if with-supertype
                             (mapcar (lambda (a)
                                       (if (string-match "#" a)
                                           a
                                         (concat a "#" supertype)))
                                     attrs)
                           attrs)))
                     (third line)))
      :test 'string=)
     'string-lessp)))

(defun tg-find-schema-line (mtype type)
  "Get the line/list of `tg-schema-alist' that corresponds to
MTYPE TYPE."
  (catch 'found
    (let ((qname (tg-unique-name type 'qualified)))
      (dolist (line tg-schema-alist)
        (when (and (eq mtype (car line))
                   (string= qname (second line)))
          (throw 'found line))))))

(defun tg-unique-name (name &optional type)
  "Given a qualified name, return the unique name and vice versa.
The optional TYPE specifies that the returned name has to be the
'unique or 'qualified name."
  (if (string-match "\\." name)
      ;; this is qualified
      (if (and type (eq type 'unique))
          ;; we want the unique name
          (gethash name tg-unique-name-hashmap)
        ;; we want the qualified name
        name)
    ;; a unique name is given
    (if (and type (eq type 'qualified))
          ;; we want the qualified name
          (gethash name tg-unique-name-hashmap)
      name)))

(defun tg-edgeclass-from (ec-name)
  (fifth (tg-find-schema-line 'EdgeClass ec-name)))

(defun tg-edgeclass-to (ec-name)
  (sixth (tg-find-schema-line 'EdgeClass ec-name)))

;;** The Mode

(define-generic-mode tg-mode
  ;; Comments
  '(("//" . nil))
  ;; Keywords
  '("AggregationClass" "Boolean" "CompositionClass" "Double" "EdgeClass"
    "EnumDomain" "Graph" "GraphClass" "Integer" "List" "Package" "RecordDomain"
    "Schema" "Set" "String" "VertexClass" "abstract" "aggregate" "from" "role"
    "to" "Map")
  ;; Additional expressions to highlight
  nil
  ;; Enable greql-mode for files matching this patterns
  '("\\.tg$")
  ;; List of functions to be run when mode is activated
  '(tg-initialize))

;;** Predicates

(defun tg-incidence-list-p ()
  (and (looking-back "<[[:digit:]- ]*")
       (looking-at "[[:digit:]- ]*>")))

(defun tg-vertex-p ()
  "Return the vertex id (as string), if on a vertex line, else return nil."
  (save-excursion
    (goto-char (line-beginning-position))
    (and (looking-at "^\\([[:digit:]]+\\)\s+[[:word:]._]+\s+<[[:digit:]- ]+>")
         (match-string-no-properties 1))))

(defun tg-edge-p ()
  "Return the edge id (as string), if on an edge line, else return nil."
  (save-excursion
    (goto-char (line-beginning-position))
    (and (not (tg-vertex-p))
         (looking-at "^\\([[:digit:]]+\\)\s+[[:word:]._]+")
         (match-string-no-properties 1))))

;;** Navigation

(defun tg-vertex-by-incidence (inc)
  "Return the buffer position of the incidence INC in some incidence list."
  (save-excursion
    (goto-char (point-min))
    (re-search-forward "^Graph\s+")
    (re-search-forward
     (concat "^[[:digit:]]+ +[[:word:]._]+ +<\\(?:[-]?[[:digit:]]+\s+\\)*"
             (regexp-quote inc)
             "[[:digit:]- ]*>") nil t 1)
    (search-backward inc)
    (point)))

(defun tg-jump (arg)
  "Jump to an appropriate position depending on position of point.

When on an incidence number, jump to the vertex that is the
That-Vertex of the incident edge.

When on an edge, jump to the vertex it is starting from.  With
prefix arg, jump to the target vertex."
  (interactive "P")
  (cond
   ((tg-incidence-list-p)
    (re-search-backward "[^[:digit:]-]" nil t 1)
    (when (looking-at "[< ]\\([[:digit:]-]+\\)")
      (let ((incnum (match-string-no-properties 1)))
        (goto-char (tg-vertex-by-incidence (if (string-match "^-" incnum)
                                               (substring incnum 1)
                                             (concat "-" incnum)))))))
   ((tg-edge-p)
    (goto-char (line-beginning-position))
    (when (looking-at "\\([[:digit:]]+\\)\s+")
      (let ((no (match-string-no-properties 1)))
        (goto-char (tg-vertex-by-incidence (if arg
                                               (concat "-" no)
                                             no))))))))

(defparameter tg-mode-map
  (let ((m (make-sparse-keymap)))
    (define-key m (kbd "C-c C-c") 'tg-jump)
    (define-key m (kbd "C-c C-d") 'eldoc-mode)
    m)
  "The keymap used in tg-mode.")

;;** Eldoc

;;*** Faces

(defface tg-attribute-father-face '((t ( :inherit font-lock-type-face :height 0.6)))
  "Face used for the forfather introducing an attribute.")

(defface tg-attribute-face '((t ( :inherit font-lock-constant-face)))
  "Face used for the forfather introducing an attribute.")

(defface tg-supertype-face '((t ( :inherit font-lock-type-face :height 0.8)))
  "Face used for supertypes.")

(defface tg-type-face '((t ( :inherit font-lock-type-face)))
  "Face used for types.")

(defface tg-metatype-face '((t ( :inherit font-lock-keyword-face)))
  "Face used for meta-types.")

;;*** Code

(defvar tg--last-thing "")
(make-variable-buffer-local 'tg--last-thing)
(defvar tg--last-doc "")
(make-variable-buffer-local 'tg--last-doc)

(defun tg-eldoc-incidence ()
  (save-excursion
    (re-search-backward "[^[:digit:]]" nil t 1)
    (when (looking-at "[^[:digit:]]\\([[:digit:]]+\\)")
      (let ((incnum (match-string-no-properties 1)))
        (goto-char (buffer-end 1))
        (re-search-backward (concat "^" incnum "[[:space:]]+") nil t 1)
        (setq tg--last-doc (buffer-substring (line-beginning-position)
                                             (line-end-position)))))))

(defun tg--eldoc-vertex-or-edge (mtype)
  "Eldoc MTYPE element at current line."
  (save-excursion
    (goto-char (line-beginning-position))
    (if (looking-at "[[:digit:]]+\s+\\([[:word:]_.]+\\)")
        (let* ((name (match-string-no-properties 1))
               (qname (save-excursion
                        (re-search-backward "^Package[[:space:]]+\\(.*\\);[[:space:]]*$" nil t 1)
                        (let ((pkg (match-string-no-properties 1)))
                          (if (and pkg (not (string= "" pkg)))
                              (concat pkg "." name)
                            name)))))
          (setq tg--last-doc (tg-eldoc-vertex-or-edge mtype qname)))
      (setq tg--last-doc nil))))

(defun tg-eldoc-vertex-or-edge (mtype type)
  "Return a doc string for MTYPE TYPE"
  (let* ((line (tg-find-schema-line mtype type))
         (name (second line))
         (supers (tg-format-list (third line) 'tg-supertype-face nil t))
         (attrs (tg-format-list (tg-all-attributes mtype name 'with-supertype)
                                'tg-attribute-face
                                'tg-attribute-father-face)))
    (concat (propertize (symbol-name mtype)
                        'face 'tg-metatype-face)
            " "
            (propertize (tg-unique-name name 'unique) 'face 'tg-type-face)
            ": "
            supers
            (if (eq mtype 'EdgeClass)
                (concat " from "
                        (propertize (tg-unique-name (tg-edgeclass-from name) 'unique)
                                    'face 'tg-type-face)
                        " to "
                        (propertize (tg-unique-name (tg-edgeclass-to name) 'unique)
                                    'face 'tg-type-face))
              "")
            " {"
            attrs
            "}")))

(defun tg-format-list (lst face1 &optional face2 alltypes)
  "Return a string representation of the given list of strings of the form
  (\"item1\" \"item2#type\")

Normal strings like item1 are propertized with FACE1.

Strings containing # are split there, and the first part is
propertized with FACE1, and the second part with FACE2.  The
second part must be a qualified type name that is made unique
here, too.

If ALLTYPES is non-nil, assume that simple entries (those without
#) are also qualified type names and make them unique."
  (let ((c (car lst)))
    (if (null c)
        ""
      (concat
       (if (string-match "#" c)
           (let ((split (split-string c "#")))
             (concat (propertize (first split) 'face face1)
                     (propertize (tg-unique-name (second split) 'unique) 'face face2)))
         (if alltypes
             (propertize (tg-unique-name c 'unique) 'face face1)
           (propertize c 'face face1)))
       (let ((reststr (tg-format-list (cdr lst) face1 face2)))
         (if (= (length reststr) 0)
             reststr
           (concat " " reststr)))))))

(defun tg-documentation-function ()
  (let ((thing (thing-at-point 'sexp)))
    (if (string= thing tg--last-thing)
        tg--last-doc
      (setq tg--last-thing thing)
      (let ((eid (tg-edge-p))
            (vid (tg-vertex-p)))
        (cond
         ((tg-incidence-list-p)
          (tg-eldoc-incidence))
         (eid
          (tg--eldoc-vertex-or-edge 'EdgeClass))
         (vid
          (tg--eldoc-vertex-or-edge 'VertexClass))
         (t
          (setq tg--last-doc nil))))
      tg--last-doc)))

(defun tg-eldoc-init ()
  (set (make-local-variable 'eldoc-documentation-function)
       'tg-documentation-function)
  (add-hook 'after-save-hook
            'tg-init-schema)
  (tg-init-schema))

;;** Init function

(defun tg-initialize ()
  (use-local-map tg-mode-map)
  (require 'eldoc)
  (add-hook 'eldoc-mode-hook
            'tg-eldoc-init nil t))

(provide 'tg-mode)

;;; tg-mode.el ends here
