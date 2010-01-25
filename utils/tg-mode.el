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
         ((looking-at "^Package[[:space:]]+\\([[:alnum:]._]+\\)")
          (let ((match (match-string-no-properties 1)))
            (setq current-package (if (string-match "^[[:space:]]*$" match)
                                      ""
                                    (concat  (match-string-no-properties 1) ".")))))
         ;; GraphClass
         ((looking-at "^GraphClass[[:space:]]+\\([[:alnum:]._]+\\)[[:space:]]*\\(?:{\\([^}]*\\)}\\)?"))
         ;; VertexClass
         ((looking-at (concat "^\\(?:abstract[[:space:]]+\\)?"
                              "VertexClass[[:space:]]+"
                              "\\([[:alnum:]._]+\\)[[:space:]]*"
                              "\\(?::\\([^{[;]+\\)\\)?[[:space:]]*" ;; Superclasses
                              "\\(?:{\\([^}]*\\)}\\)?"     ;; Attributes
                              "\\(?:[[].*[]]\\)*[[:space:]]*;"      ;; Constraints
                              ))
          (let ((qname (concat current-package (match-string-no-properties 1))))
            (setq schema-alist
                  (cons (list :meta 'VertexClass
                              :qname qname
                              :super (tg--parse-superclasses (match-string-no-properties 2) current-package)
                              :attrs (tg--parse-attributes qname (match-string-no-properties 3)))
                        schema-alist))))
         ;; EdgeClasses
         ((looking-at (concat "^\\(?:abstract[[:space:]]+\\)?"
                              "\\(\\(?:Edge\\|Aggregation\\|Composition\\)Class\\)[[:space:]]+"
                              "\\([[:alnum:]._]+\\)[[:space:]]*" ;; Name
                              "\\(?::\\([[:alnum:]._ ]+\\)\\)?[[:space:]]*" ;; Supertypes
                              "\\<from\\>[[:space:]]+\\([[:alnum:]._]+\\)[[:space:]]+.*\\<to\\>[[:space:]]+\\([[:alnum:]._]+\\)[[:space:]]+.*" ;; from/to
                              "\\(?:{\\([^}]*\\)}\\)?" ;; Attributes
                              "\\(?:[[].*[]]\\)*[[:space:]]*;"  ;; Constraints
                              ))
          (let ((qname (concat current-package (match-string-no-properties 2)))
                (from (match-string-no-properties 4))
                (to   (match-string-no-properties 5)))
            (save-match-data
              (setq from (if (string-match "\\." from) from (concat current-package from)))
              (setq to   (if (string-match "\\." to)   to   (concat current-package to))))
            (setq schema-alist
                  (cons (list :meta 'EdgeClass
                              :qname qname
                              :super (tg--parse-superclasses (match-string-no-properties 3) current-package)
                              :attr (tg--parse-attributes qname (match-string-no-properties 6))
                              :edgetype (match-string-no-properties 1)
                              :from from
                              :to to)
                        schema-alist))))
         ;; End of schema (part)
         ((or (= (point) (point-max))
              (looking-at "^Graph[[:space:]]+"))
          (setq finished t)))
        (forward-line 1))
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

(defun tg--parse-attributes (qname str)
  "Parse STR to an attribute list."
  (when str
    (save-match-data
      (setq str (replace-regexp-in-string "[[:space:]]+" "" str))
      (let ((list (split-string str ","))
            result)
        (dolist (elem list)
          (let ((split (split-string elem ":")))
            (setq result (cons (list :name (car split)
                                     :domain (cadr split)
                                     :owner qname)
                               result))))
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
    (let* ((qname (plist-get l :qname))
           (uname (replace-regexp-in-string "\\(?:.*\\.\\)\\([^.]+\\)" "\\1" qname)))
      (if (gethash uname tg-unique-name-hashmap)
          ;; Not unique, remove the old entry, too
          (let ((oldqname (gethash uname tg-unique-name-hashmap)))
            (remhash uname tg-unique-name-hashmap)
            (puthash oldqname oldqname tg-unique-name-hashmap)
            (puthash qname qname tg-unique-name-hashmap))
        (puthash uname qname tg-unique-name-hashmap)
        (puthash qname uname tg-unique-name-hashmap)))))

;;** Schema querying

(defun tg--attribute-name-member-p (a lst)
  "Return non-nil, if the attribute list LST contains some
attribute, which is named like the attribute A.  Attributes are
plists (:name \"a\" :domain \"d\" :owner \"o\")."
  (catch 'found
    (dolist (e lst)
      (when (string= (plist-get e :name) (plist-get a :name))
        (throw 'found t)))))

(defun tg--attribute-restriction (lists)
  "Gets a list of lists, where each list is the list of
attributes from some schema element.  Returns a list of common
attributes, where common means, that only the attribute names
have to equal, but not the domain or owner."
  (let (result)
    (dolist (attr (reduce 'append lists))
      (let ((in-all (catch 'in-all
                      (dolist (type lists)
                        (when (not (tg--attribute-name-member-p attr type))
                          (throw 'in-all nil)))
                      (throw 'in-all t))))
        (when (and in-all (not (member attr result)))
          (setq result (cons attr result)))))
    result))

(defun tg-all-attributes-multi (mtype types &optional only-in-all)
  "Returns a list of all attributes that are defined in all TYPES
of meta type MTYPE.

If ONLY-IN-ALL is non-nil, restrict them to attributes that are
valid for all TYPES."
  (let ((all-attrs (mapcar
                    (lambda (type)
                      (tg-all-attributes (tg-get-schema-element mtype type)))
                    types)))
    (if only-in-all
        (tg--attribute-restriction all-attrs)
      (remove-duplicates
       (reduce 'nconc all-attrs )))))

(defun tg-all-attributes (elem)
  "Returns an alist of all attribute of the schema element
ELEM (and its supertypes)."
  (sort
   (remove-duplicates
    (apply 'append (plist-get elem :attrs)
           (mapcar
            (lambda (supertype)
              (tg-all-attributes (tg-get-schema-element
                                  (plist-get elem :meta)
                                  supertype)))
            (plist-get elem :super)))
    :test (lambda (a1 a2)
            (string= (plist-get a1 :name) (plist-get a2 :name))))
   (lambda (a1 a2)
     (string-lessp (plist-get a1 :name) (plist-get a2 :name)))))

(defun tg-get-schema-element (meta name)
  "Get the line/list of `tg-schema-alist' that corresponds to the
meta type META and has the name NAME.  NAME may be qualified or
unique."
  (catch 'found
    (let ((qname (tg-unique-name name 'qualified)))
      (dolist (elem tg-schema-alist)
        (when (and (eq meta (plist-get elem :meta))
                   (string= qname (plist-get elem :qname)))
          (throw 'found elem))))))

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
    (and (looking-at "^\\([[:digit:]]+\\)[[:space:]]+[[:word:]._]+[[:space:]]+<[[:digit:]- ]*>")
         (match-string-no-properties 1))))

(defun tg-edge-p ()
  "Return the edge id (as string), if on an edge line, else return nil."
  (save-excursion
    (goto-char (line-beginning-position))
    (and (not (tg-vertex-p))
         (looking-at "^\\([[:digit:]]+\\)[[:space:]]+[[:word:]._]+")
         (match-string-no-properties 1))))

;;** Navigation

(defun tg-vertex-by-incidence (inc)
  "Return the buffer position of the incidence INC in some incidence list."
  (save-excursion
    (goto-char (point-min))
    (re-search-forward "^Graph[[:space:]]+")
    (re-search-forward
     (concat "^[[:digit:]]+ +[[:word:]._]+ +<\\(?:[-]?[[:digit:]]+[[:space:]]+\\)*"
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
    (when (looking-at "\\([[:digit:]]+\\)[[:space:]]+")
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

(defface tg-attribute-father-face '((t ( :inherit font-lock-type-face :height 0.7)))
  "Face used for the forfather introducing an attribute.")

(defface tg-attribute-face '((t ( :inherit font-lock-constant-face)))
  "Face used for the forfather introducing an attribute.")

(defface tg-supertype-face '((t ( :inherit font-lock-type-face :height 0.85)))
  "Face used for supertypes.")

(defface tg-type-face '((t ( :inherit font-lock-type-face)))
  "Face used for types.")

(defface tg-metatype-face '((t ( :inherit font-lock-keyword-face)))
  "Face used for meta-types.")

(defface tg-keyword-face '((t ( :inherit font-lock-keyword-face)))
  "Face used for keywords.")

;;*** Code

(defvar tg--last-thing "")
(make-variable-buffer-local 'tg--last-thing)
(defvar tg--last-doc "")
(make-variable-buffer-local 'tg--last-doc)

(defun tg-eldoc-incidence ()
  "Return a doc string for the incidence at point."
  (save-excursion
    (re-search-backward "[^[:digit:]]" nil t 1)
    (when (looking-at "[^[:digit:]]\\([[:digit:]]+\\)")
      (let ((incnum (match-string-no-properties 1)))
        (goto-char (buffer-end 1))
        (re-search-backward (concat "^" incnum "[[:space:]]+") nil t 1)
        (setq tg--last-doc (buffer-substring (line-beginning-position)
                                             (line-end-position)))))))

(defun tg-eldoc-vertex-or-edge-at-point (mtype)
  "Eldoc MTYPE element at current line."
  (save-excursion
    (goto-char (line-beginning-position))
    (if (looking-at "[[:digit:]]+[[:space:]]+\\([[:word:]_.]+\\)")
        (let* ((name (match-string-no-properties 1))
               (qname (save-excursion
                        (re-search-backward "^Package[[:space:]]+\\(.*\\);[[:space:]]*$" nil t 1)
                        (let ((pkg (match-string-no-properties 1)))
                          (if (and pkg (not (string= "" pkg)))
                              (concat pkg "." name)
                            name)))))
          (setq tg--last-doc (tg-eldoc-vertex-or-edge (tg-get-schema-element mtype qname))))
      (setq tg--last-doc nil))))

(defun tg-eldoc-vertex-or-edge (elem)
  "Return a doc string for schema element ELEM."
  (let* ((mtype (plist-get elem :meta))
         (name (plist-get elem :qname))
         (supers (tg-format-type-list (plist-get elem :super) 'tg-supertype-face))
         (attrs (tg-format-attr-list (tg-all-attributes elem)
                                     'tg-attribute-face
                                     'tg-type-face
                                     'tg-supertype-face)))
    (concat (propertize (if (eq mtype 'EdgeClass)
                            (plist-get elem :edgetype)
                          (symbol-name mtype))
                        'face 'tg-metatype-face)
            " "
            (propertize (tg-unique-name name 'unique) 'face 'tg-type-face)
            (if (= (length supers) 0) "" (concat ": " supers))
            (if (eq mtype 'EdgeClass)
                (concat (propertize " from " 'face 'tg-keyword-face)
                        (propertize (tg-unique-name (plist-get elem :from) 'unique)
                                    'face 'tg-type-face)
                        (propertize " to " 'face 'tg-keyword-face)
                        (propertize (tg-unique-name (plist-get elem :to) 'unique)
                                    'face 'tg-type-face))
              "")
            " {"
            attrs
            "}")))

(defun tg-format-type-list (lst face)
  "Return a string representation of the given list of type names
propertized with FACE.  If the types are unique, their unique
name is used."
  (let ((c (car lst)))
    (if (null c)
        ""
      (concat
       (propertize (tg-unique-name c 'unique) 'face face)
       (let ((reststr (tg-format-type-list (cdr lst) face)))
         (if (= (length reststr) 0)
             reststr
           (concat ", " reststr)))))))

(defun tg-format-attr-list (lst face1 face2 face3)
  "Return a string representation of the given attribute list:
IN: ((\"attr1\" \"domain1\" \"OwningType\") (\"attr2\" \"domain2\"))
OUT: attr1 : domain1 (OwningType), attr2 : domain2

Attributes are propertized using FACE1, domains with FACE2, and
types with FACE3."
  (let ((c (car lst)))
    (if (null c)
        ""
      (concat
       (propertize (plist-get c :name) 'face face1)
       ":"
       (propertize (plist-get c :domain) 'face face2)
       (when (plist-get c :owner)
         (concat "(" (propertize (tg-unique-name (plist-get c :owner) 'unique) 'face face3) ")"))
       (let ((reststr (tg-format-attr-list (cdr lst) face1 face2 face3)))
         (if (= (length reststr) 0)
             reststr
           (concat ", " reststr)))))))

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
          (tg-eldoc-vertex-or-edge-at-point 'EdgeClass))
         (vid
          (tg-eldoc-vertex-or-edge-at-point 'VertexClass))
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
