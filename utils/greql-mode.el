;;; greql-mode.el --- Major mode for editing GReQL2 files with emacs

;; Copyright (C) 2007, 2008 by Tassilo Horn

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

;; Major mode for editing GReQL2 files with Emacs and executing queries.


;;; Version:
;; <2008-11-14 Fri 16:09>

;;; Code:

(defvar greql-keywords
  '("E" "V" "as" "bag" "eSubgraph" "end" "exists!" "exists" "forall"
    "from" "in" "let" "list" "path" "pathSystem" "rec" "report"
    "reportBag" "reportSet" "set" "store" "tup" "using" "vSubgraph"
    "where" "with")
  "GReQL keywords that should be highlighted.")

(put 'greql-keywords 'risky-local-variable-p t)

(defvar greql-functions
  '("and" "avg" "children" "contains" "count" "degree" "depth"
    "difference" "distance" "dividedBy" "edgesConnected" "edgesFrom"
    "edgesTo" "edgeTrace" "edgeTypeSet" "endVertex" "equals"
    "extractPath" "getEdge" "getValue" "getVertex" "grEqual" "grThan"
    "hasAttribute" "hasType" "id" "inDegree" "innerNodes"
    "intersection" "isAcyclic" "isA" "isCycle" "isIn" "isIsolated"
    "isLoop" "isNeighbour" "isParallel" "isPrime" "isReachable"
    "isSibling" "isSubPathOfPath" "isSubSet" "isSuperSet" "isTrail"
    "isTree" "leaves" "leEqual" "leThan" "matches" "maxPathLength"
    "minPathLength" "minus" "modulo" "nequals" "nodeTrace" "not"
    "nthElement" "or" "outDegree" "parent" "pathConcat" "pathLength"
    "pathSystem" "plus" "pos" "reachableVertices" "reMatch"
    "schemaFunctions" "siblings" "squareRoot" "startVertex"
    "subtypes" "sum" "supertypes" "symDifference" "times" "toString"
    "type" "typeName" "typeSet" "types" "uminus" "union"
    "vertexTypeSet" "weight" "xor")
  "GReQL functions that should be highlighted.")

(put 'greql-functions 'risky-local-variable-p t)

(dolist (ext '("\\.greqlquery$" "\\.grq$" "\\.greql$"))
  (add-to-list 'auto-mode-alist (cons ext 'greql-mode)))

(defvar greql-tab-width 2
  "Distance between tab stops (for display of tab characters), in columns.")

(defvar greql-script-program "~/bin/greqlscript"
  "The program to execute GReQL queries.")

(define-derived-mode greql-mode text-mode "GReQL"
  "A major mode for GReQL2."
  ;; Comments
  (setq comment-start "//"
	comment-start-skip "/\\*"
	comment-end "\\*/")
  ;; Keywords
  (setq font-lock-defaults
	(let (list)
	  (push (concat "\\_<" (regexp-opt greql-keywords t) "\\_>")
		list)
	  (push (cons (concat "\\_<" (regexp-opt greql-functions t) "\\_>")
		      font-lock-builtin-face)
		list)
	  (list list)))

  (setq tab-width greql-tab-width)
  ;; List of functions to be run when mode is activated
  (define-key greql-mode-map (kbd "M-TAB")   'greql-complete)
  (define-key greql-mode-map (kbd "C-c C-v") 'greql-complete-vertexclass)
  (define-key greql-mode-map (kbd "C-c C-e") 'greql-complete-edgeclass)
  (define-key greql-mode-map (kbd "C-c C-d") 'greql-complete-domain)
  (define-key greql-mode-map (kbd "C-c C-s") 'greql-set-graph)
  (define-key greql-mode-map (kbd "C-c C-c") 'greql-execute))

(defvar greql-graph nil
  "The graph which is used to extract schema information on which
queries are evaluated.  Set it with `greql-set-graph'.")

(make-variable-buffer-local 'greql-graph)

(defvar greql-schema-alist nil)
(make-variable-buffer-local 'greql-schema-alist)

(defun greql-set-graph (graph)
  "Set `greql-graph' to GRAPH and parse it with
`greql-parse-schema'."
  (interactive "fGraph file: ")
  (setq greql-graph graph)
  (let ((g greql-graph)
	schema-alist)
    (with-temp-buffer
      (insert-file-contents g)
      (setq schema-alist (greql-parse-schema)))
    (setq greql-schema-alist schema-alist))
  ;; add keywords and functions, too
  (dolist (key greql-keywords)
    (setq greql-schema-alist (cons (list 'keyword key)
				   greql-schema-alist)))
  (dolist (fun greql-functions)
    (setq greql-schema-alist (cons (list 'funlib fun)
				   greql-schema-alist))))

(defun greql-parse-schema ()
  "Parse `greql-graph' and extract schema information into
`greql-schema-alist'."
  (goto-char (point-min))
  (let ((current-package "")
	schema-alist)
    (while (re-search-forward
	    (rx bol ;; Anchor at the beginning of line.
		(and
		 ;; First, there may be the keyword abstract followed by
		 ;; whitespaces.
		 (zero-or-one "abstract")
		 (zero-or-more (syntax whitespace))
		 ;; Now comes the Meta-Meta-Model type.  We need it later, so
		 ;; extract it.
		 (group
		  (or "VertexClass" "Package" "AggregationClass" "CompsitionClass"
		      "EdgeClass" "Schema" "GraphClass"
		      (and (one-or-more (or (syntax word) (any "_."))) "Domain")))
		 ;; Then there may be whitespaces again...
		 (zero-or-more (syntax whitespace))
		 ;; Now comes the Meta-Model type name.
		 (or (group (one-or-more (or (syntax word) (any "_.")))) ";")
		 ;; Superclasses
		 (zero-or-one
		  (and (zero-or-more (syntax whitespace)) ":" (zero-or-more (syntax whitespace))
		       (minimal-match (and (group (and (one-or-more anything) (not (any ",;")))) 
					   (any " ;")))))
		 ;; Attributes
		 (zero-or-one (and (zero-or-more (not (any "\n")))
				   "{" (group (zero-or-more (not (any "\n")))) "}"))
		 ))
	    nil t)
      (let ((vc-or-pkg (match-string 1))
	    (name (match-string 2))
	    (superclasses (greql-parse-superclasses (match-string 3)))
	    (attributes (greql-parse-attributes (match-string 4))))
	(cond ((string= vc-or-pkg "Package")
	       ;; All following elements belong to this package, so make it
	       ;; current.
	       (setq current-package (if name (concat name ".") "")))
	      ((string= vc-or-pkg "VertexClass")
	       (setq schema-alist (cons (list (intern vc-or-pkg)
					      (concat current-package name)
					      superclasses
					      attributes)
					schema-alist)))
	      ((or (string= vc-or-pkg "EdgeClass")
		   (string= vc-or-pkg "AggregationClass")
		   (string= vc-or-pkg "CompositionClass"))
	       (setq schema-alist (cons (list 'EdgeClass
					      (concat current-package name)
					      superclasses
					      attributes)
					schema-alist)))
	      (t
	       ;; This must be a domain
	       (setq schema-alist (cons (list (intern vc-or-pkg)
					      (concat current-package name))
					schema-alist))))))
    schema-alist))

(defun greql-parse-superclasses (str)
  "Given a string \": Foo, Bar, Baz\" it returns (\"Foo\" \"Bar\"
\"Baz\")"
  (when str
    (save-match-data
      (split-string str "[ ,]+"))))

(defun greql-parse-attributes (str)
  (when str
    (save-match-data
      (let ((list (split-string str "[ :,]+"))
	    result
	    (i 1))
	(dolist (elem list)
	  (when (= (mod i 2) 1)
	    (setq result (cons elem result)))
	  (setq i (+ i 1)))
	result))))

(defun greql-completion-list (&optional types)
  (when greql-schema-alist
    (let (completions)
      (dolist (line greql-schema-alist)
	(when (or (null types) (member (car line) types))
	  (let ((elem (cadr line)))
	    (setq completions (cons elem completions)))))
      completions)))

(defun greql-complete-1 (&optional types)
  (let* ((window (get-buffer-window "*Completions*" 0))
	 (beg (save-excursion
		(+ 1 (or (re-search-backward "[^[:word:]._]" nil t) 0))))
	 (word (buffer-substring-no-properties beg (point)))
	 (compl (try-completion word
				(greql-completion-list types))))
    (if (and (eq last-command this-command)
	     window (window-live-p window) (window-buffer window)
	     (buffer-name (window-buffer window)))
	;; If this command was repeated, and there's a fresh completion window
	;; with a live buffer, and this command is repeated, scroll that
	;; window.
	(with-current-buffer (window-buffer window)
	  (if (pos-visible-in-window-p (point-max) window)
	      (set-window-start window (point-min))
	    (save-selected-window
	      (select-window window)
	      (scroll-up))))
      (cond
       ((null compl)
	(message "No completion possible.  Did you set a graph?"))
       ((stringp compl)
	(if (string= word compl)
	    ;; Show completion buffer
	    (let ((list (all-completions word
					 (greql-completion-list types))))
	      (setq list (sort list 'string<))
	      (with-output-to-temp-buffer "*Completions*"
		    (display-completion-list list word)))
	  ;; Complete
	  (delete-region beg (point))
	  (insert compl)
	  ;; close completion buffer if there's one
	  (let ((win (get-buffer-window "*Completions*" 0)))
	    (if win (quit-window nil win)))))
       (t (message "That's the only possible completion."))))))

(defun greql-complete ()
  "Complete word at point somehow intelligently."
  (interactive)
  (cond
   ((greql-vertex-set-expression-p) (greql-complete-vertexclass))
   ((or (greql-edge-set-expression-p)
	(greql-edge-restriction-p))
    (greql-complete-edgeclass))
   ((greql-variable-p)
    (let* ((vartypes (greql-variable-types))
	   (attrs (greql-attributes vartypes)))
      (message "vartypes = %s, attrs = %s" vartypes attrs)
      ))
   (t (greql-complete-keyword-or-function))))

(defun greql-complete-vertexclass ()
  (interactive)
  (greql-complete-1 '(VertexClass)))

(defun greql-complete-edgeclass ()
  (interactive)
  (greql-complete-1 '(EdgeClass)))

(defun greql-complete-domain ()
  (interactive)
  (greql-complete-1 '(EnumDomain RecordDomain ListDomain SetDomain BagDomain)))

(defun greql-complete-keyword-or-function ()
  (interactive)
  (greql-complete-1 '(keyword funlib)))

(defun greql-execute ()
  "Execute the query in the current buffer on `greql-graph'."
  (interactive)
  (let ((buffer (get-buffer-create "*GReQL*"))
	(evalstr (buffer-substring-no-properties (point-min) (point-max))))
    (with-current-buffer buffer
      (erase-buffer))
    (start-process "GReQL process" buffer
		   greql-script-program
		   "-e" evalstr
		   "-g" (expand-file-name greql-graph))
    (display-buffer buffer)))

(defun greql-vertex-set-expression-p ()
  (looking-back "V{[[:word:]._, ]*"))

(defun greql-edge-set-expression-p ()
  (looking-back "E{[[:word:]._, ]*"))

(defun greql-edge-restriction-p ()
  (looking-back "--{[[:word:]._, ]*"))

(defun greql-variable-p ()
  (looking-back "[^{][[:word:]]+[.][[:word:]]*"))

(defun greql-variable-types ()
  "Return something like (VertexClass (Type1 Type2 Type3)."
  (save-excursion
    (search-backward "." nil t 1)
    (let ((end (point))
	  var)
      (search-backward " " nil t 1)
      (setq var (buffer-substring-no-properties (+ 1 (point)) end))
      (re-search-backward (concat var "[ ]*:[ ]*\\([VE]\\){\\(.*\\)}") nil t 1)
      (let* ((mtype-match (buffer-substring-no-properties (match-beginning 1)
							  (match-end 1)))
	     (mtype (cond 
		     ((or (not mtype-match) (string= mtype-match "E")) 'EdgeClass)
		     ((string= mtype-match "V") 'VertexClass)
		     (t (error "Not match!"))))
	     (types (buffer-substring-no-properties (match-beginning 2)
						    (match-end 2))))
	(list mtype (split-string types ", "))))))

(defun greql-attributes (typelist)
  (remove-duplicates
   (greql-attributes-1 (car typelist) (cadr typelist))
   :test 'string=))

(defun greql-attributes-1 (mtype types)
  (let (attrs)
    (dolist (line greql-schema-alist)
      (when (and (eq mtype (car line))
		 (member (cadr line) types))
	(setq attrs
	      (append attrs (fourth line)
		      (greql-attributes-1 mtype (third line))))))
    attrs))

(provide 'greql-mode)

;;; greql-mode.el ends here
