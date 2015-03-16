;;; greql-mode.el --- Major mode for editing GReQL2 and GReTL files with emacs

;; Copyright (C) 2007, 2008, 2009, 2010, 2011, 2015 by Tassilo Horn

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

;; Major mode for editing GReQL2 files with Emacs and executing queries.  Also
;; some stuff for editing GReTL transformations.

;;* Code

(require 'cl-lib)

;;** Main

;; TG mode contains the schema parsing stuff.
(require 'tg-mode)

(defvar greql-keywords
  (let ((lst '((:meta keyword :name "E" :description "EdgeSetExpression: E{<Type>+}")
               (:meta keyword :name "V" :description "VertexSetExpression: V{<Type>+}")
               (:meta keyword :name "as" :description "Assign a name to a table header.")
               (:meta keyword :name "eSubgraph" :description "EdgeSubgraphExpression: eSubgraph(<EdgeType>+)")
               (:meta keyword :name "end" :description "Comprehension: from [with <Exp>+] report <Exp>+ end")
               (:meta keyword :name "exists!" :description "QuantifiedExpression: exists! <Decl>+ @ <Exp>")
               (:meta keyword :name "exists" :description "QuantifiedExpression: exists <Decl>+ @ <Exp>")
               (:meta keyword :name "forall" :description "QuantifiedExpression: forall <Decl>+ @ <Exp>")
               (:meta keyword :name "from" :description "Comprehension: from <Decl> [with <Exp>+] report <Exps> end")
               (:meta keyword :name "in" :description "LetExpression: let <<Name> := <Exp>>+ in <Exp>")
               (:meta keyword :name "let" :description "LetExpression: let <<Name> := <Exp>>+ in <Exp>")
               (:meta keyword :name "list" :description "ListConstruction: list(<Exp>+|<Range>)")
               (:meta keyword :name "path" :description "")
               (:meta keyword :name "pathsystem" :description "")
               (:meta keyword :name "rec" :description "RecordConstruction: rec(<<Name> := <Exp>>+)")
               (:meta keyword :name "report" :description "TableComprehension: from <Decl> [with <Exp>+] report <<Exp> [as <HdrName>]>+ end")
               (:meta keyword :name "reportList" :description "ListComprehension: from <Decl> [with <Exp>+] reportList <Exp>+ end")
	       (:meta keyword :name "reportListN" :description "Limited ListComprehension: from <Decl> [with <Exp>+] reportListN <IntExp>: <Exp>+ end")
               (:meta keyword :name "reportSet" :description "SetComprehension: from <Decl> [with <Exp>+] reportSet <Exp>+ end")
	       (:meta keyword :name "reportSetN" :description "Limited SetComprehension: from <Decl> [with <Exp>+] reportSetN <IntExp>: <Exp>+ end")
               (:meta keyword :name "reportMap" :description "MapComprehension: from <Decl> [with <Exp>+] reportMap <KeyExp>, <ValueExp> end")
	       (:meta keyword :name "reportMapN" :description "Limited MapComprehension: from <Decl> [with <Exp>+] reportMapN <IntExp>: <KeyExp>, <ValueExp> end")
               (:meta keyword :name "set" :description "SetConstruction: set(<Exp>+)")
               (:meta keyword :name "store" :description "Store result as XML")
               (:meta keyword :name "tup" :description "TupleConstruction: tup(<Exp>+)")
               (:meta keyword :name "using" :description "Use global variables: using <VarName>+;")
               (:meta keyword :name "vSubgraph" :description "VertexSubgraphExpression: vSubgraph(<VertexType>+)")
               (:meta keyword :name "where" :description "WhereExpression: <Exp> where <<Name> := <Exp>>+")
               (:meta keyword :name "with" :description "Comprehension: from <Decl> [with <Exp>+] report <Exp>+ end")
               (:meta keyword :name "thisEdge" :description "ThisEdge: The currently iterated edge.")
               (:meta keyword :name "thisVertex" :description "ThisVertex: The currently iterated vertex.")
               (:meta keyword :name "map" :description "MapConstruction: map(<<KeyExp> -> <ValueExp>>+)")
               (:meta keyword :name "import" :description "ImportStatement: import <Type>|<Wildcart>")
               (:meta keyword :name "true" :description "Logically true")
               (:meta keyword :name "false" :description "Logically false"))))
    (let ((rx (concat "\\_<" (regexp-opt
			      (mapcar (lambda (e) (plist-get e :name))
				      lst)
			      t)
                      "\\_>"))
          lst2)
      (dolist (kw lst)
        (setq lst2 (cons (list
                          :meta (plist-get kw :meta)
                          :name (plist-get kw :name)
                          :description (with-temp-buffer
                                         (insert (plist-get kw :description))
                                         (goto-char (point-min))
                                         (while (re-search-forward rx nil t)
                                           (put-text-property (match-beginning 1)
                                                              (match-end 1) 'face 'bold))
                                         (buffer-substring (point-min) (point-max))))
                         lst2)))
      lst2))
  "GReQL keywords that should be completed and highlighted.")
(put 'greql-keywords 'risky-local-variable-p t)

(defvar greql-jgralab-jar-file
  "/home/horn/uni/repos/jgralab/build/jar/jgralab.jar")

(defun greql-functions ()
  "Return a list of all available GReQL function lists."
  (with-temp-buffer
    (call-process "java" nil
                  (current-buffer)
                  nil
                  "-cp" greql-jgralab-jar-file
                  "de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary"
                  "-l")
    (goto-char (point-min))
    (let (list)
      (while (re-search-forward "^\\([[:word:]]+\\):[[:space:]]*\\([^[:space:]].*\\)$" nil t)
        (setq list (cons (list :meta 'function
                               :name (match-string-no-properties 1)
                               :description (match-string-no-properties 2))
                         list)))
      (nreverse list))))

(defun greql-function-p ()
  "Return a function plist if point is on a function."
  (let ((fun (current-word t t)))
    (and (looking-at "[[:word:]]*(")
         (catch 'fun
           (dolist (f greql-functions)
             (when (string= (plist-get f :name) fun)
               (throw 'fun f)))))))

(defun greql-keyword-p ()
  "Return a keyword plist if point is on a keyword."
  (let ((key (current-word t t)))
    (catch 'keyword
      (dolist (k greql-keywords)
        (when (string= (plist-get k :name) key)
          (throw 'keyword k))))))

(defvar greql-functions (greql-functions)
  "GReQL functions that should be completed and highlighted.")

(dolist (ext '("\\.greqlquery$" "\\.grq$" "\\.greql$" "\\.gretl$"))
  (add-to-list 'auto-mode-alist (cons ext 'greql-mode)))

(defvar greql-fontlock-keywords-1
  `(
    ;; Highlight function names
    ,(list (regexp-opt (mapcar (lambda (f)
				 (plist-get f :name))
			       greql-functions)
		       'symbols)
           1 font-lock-function-name-face)
    ;; Highlight strings
    ,(list "[^\\]\\(\".*?[^\\]\"\\)" 1 font-lock-string-face t)
    ,(list "[^\\]\\('.*?[^\\]'\\)"   1 font-lock-string-face t)
    ;; Highlight one-line comments
    ,(list "//.*$" 0 font-lock-comment-face t)))

(defvar greql-fontlock-keywords-2
  (append greql-fontlock-keywords-1
          (list (regexp-opt (mapcar (lambda (key) (plist-get key :name))
                                    greql-keywords)
                            'words))))

(defun greql-fontlock-known-types (limit)
  (catch 'found
    (while (re-search-forward "[{,]" limit t)
      (when (and greql-fontlock-types-regex
                 (not (string= greql-fontlock-types-regex ""))
                 (looking-back "{[[:alnum:][:space:]^.!_,]*")
                 (looking-at   "[[:alnum:][:space:]^.!_,]*\\(?:@.*\\)?}")
                 (looking-at (concat "[[:space:]]*\\_<" greql-fontlock-types-regex "\\_>"))
                 (match-string 1))
        (goto-char (match-end 1))
        (throw 'found t)))))

(defun greql-fontlock-unknown-types (limit)
  (catch 'found
    (while (re-search-forward "[{,]" limit t)
      (when (and (looking-back "{[[:alnum:][:space:]^.!_,]*")
                 (looking-at   "[[:alnum:][:space:]^.!_,]*\\(?:@.*\\)?}")
                 (or greql-fontlock-types-regex
                     (not (string= greql-fontlock-types-regex ""))
                     (not (looking-at (concat "[[:space:]^]*\\_<" greql-fontlock-types-regex "\\_>")))))
        (looking-at "[[:space:]^]*\\_<\\([[:alnum:]._]+\\)\\_>")
        (goto-char (match-end 1))
        (throw 'found t)))))

(defvar greql-fontlock-keywords-3
  (append greql-fontlock-keywords-2
          (list
           (list 'greql-fontlock-known-types 1 font-lock-type-face)
           (list 'greql-fontlock-unknown-types 1 font-lock-warning-face))))

(defvar greql-fontlock-types-regex nil)
(make-variable-buffer-local 'greql-fontlock-types-regex)

(defun greql-set-fontlock-types-regex ()
  (setq greql-fontlock-types-regex
        (regexp-opt
         (mapcar
          (lambda (elem) (car elem))
          (append
           (save-excursion
             (goto-char (point-max))
             (greql-import-completion-list '(EdgeClass VertexClass)))
           (greql-completion-list '(EdgeClass VertexClass))))
         t)))

(defvar greql-tab-width 2
  "Distance between tab stops (for display of tabs), in columns.")

(defvar greql-evaluation-buffer nil
  "Name of the GReQL evaluation buffer.")
(make-variable-buffer-local 'greql-evaluation-buffer)

(declare-function 'gretl-minor-mode (buffer-file-name))

(define-derived-mode greql-mode text-mode "GReQL"
  "A major mode for GReQL2."
  ;; Comments
  (set (make-local-variable 'comment-start)      "//")
  (set (make-local-variable 'comment-end)        "")

  ;; Keywords
  (setq font-lock-defaults
        '((greql-fontlock-keywords-1
           greql-fontlock-keywords-2
           greql-fontlock-keywords-3)))
  (greql-set-fontlock-types-regex)

  ;; Update highlighting of Vertex- and EdgeClasses after saving
  (add-hook 'after-save-hook 'greql-set-fontlock-types-regex t t)

  (setq tab-width greql-tab-width)
  (set (make-local-variable 'indent-line-function) 'greql-indent-line)
  (set (make-local-variable 'eldoc-documentation-function)
       'greql-documentation-function)

  (setq greql-evaluation-buffer (concat "*GReQL Evaluation: " (buffer-name) "*"))

  ;; If this is a GReTL buffer, enable gretl-minor-mode, too.
  (when (string-match "\\.gretl$"
		      (or (buffer-file-name) (buffer-name)))
    (gretl-minor-mode 1))

  (progn
    (define-key greql-mode-map (kbd "M-TAB")   'greql-complete)
    (define-key greql-mode-map (kbd "C-c SPC") 'greql-complete)
    (define-key greql-mode-map (kbd "C-c C-d") 'greql-show-documentation)
    (define-key greql-mode-map (kbd "C-c C-s") 'greql-set-graph)
    (define-key greql-mode-map (kbd "C-c C-c") 'greql-execute)
    (define-key greql-mode-map (kbd "C-c C-f") 'greql-format)

    ;; Menu bar
    (define-key greql-mode-map [menu-bar greql]
      (cons "GReQL" (make-sparse-keymap "GReQL")))

    (define-key greql-mode-map [menu-bar greql exec-query]
      '(menu-item "Execute query" greql-execute
                  :help "Execute the current query and show the results"))
    (define-key greql-mode-map [menu-bar greql format-query]
      '(menu-item "Format query" greql-format
                  :help "Auto-format the current query"))
    (define-key greql-mode-map [menu-bar greql show-doc]
      '(menu-item "Show docs" greql-show-documentation
                  :help "Show the documentation for the element at point"))
    (define-key greql-mode-map [menu-bar greql complete]
      '(menu-item "Complete element" greql-complete
                  :help "Complete the element at point"))
    (define-key greql-mode-map [menu-bar greql set-graph]
      '(menu-item "Set Graph/Schema" greql-set-graph
                  :help "Set the graph/schema for the current query"))))

(defvar greql-graph nil
  "The graph used for querying and schema extraction.
Set it with `greql-set-graph'.")
(make-variable-buffer-local 'greql-graph)

(defun greql-set-graph (graph)
  "Set `greql-graph' to GRAPH and parse it with `tg-parse-schema'."
  (interactive "fGraph file: ")
  (setq greql-graph graph)
  (let ((g greql-graph)
        schema-alist unique-name-map)
    (with-temp-buffer
      (insert-file-contents g)
      (tg-init-schema)
      (setq schema-alist tg-schema-alist)
      (setq unique-name-map tg-unique-name-hashmap))
    (setq tg-schema-alist schema-alist)
    (setq tg-unique-name-hashmap unique-name-map))
  ;; Setup schema element font locking
  (greql-set-fontlock-types-regex)
  ;; Clear the completions cache
  (setq greql--completion-cache nil))

(defun greql-import-completion-list (mtypes)
  "Return a completion list for imported MTYPE elements.
If the package foo is imported, then the element \"Bar\" will be
in the result, supplementing its qualified name foo.Bar gathered
by normal completion."
  (when tg-schema-alist
    (let ((comp-lst (greql-completion-list mtypes))
          lst)
      (save-excursion
        (goto-char (line-end-position))
        (while (re-search-backward "import[[:space:]]+\\([^;]+\\);" nil t)
          (unless (looking-back ".*//.*")
            (let ((import (match-string-no-properties 1)))
              (if (string-match "\\*$" import)
                  ;; A package import
                  (let ((regex (regexp-quote (substring import 0 (- (length import) 1)))))
                    (setq lst
                          (append
                           lst
                           (remove
                            nil
                            (mapcar
                             (lambda (elem)
                               (if (string-match regex (car elem))
                                   (list (replace-regexp-in-string regex "" (car elem))
                                         (cadr elem))
                                 nil))
                             comp-lst)))))
                ;; An element import
                (let ((elem (catch 'elem (dolist (m comp-lst)
                                           (when (string= (car m) import)
                                             (throw 'elem m))))))
                  (when elem
                    (setq lst (cons
                               (cons (replace-regexp-in-string
                                      "\\([[:word:]._]+\\.\\)\\([^.]+\\)" "\\2" import)
                                     (cdr elem))
                               lst)))))))))
      lst)))

(defvar greql--completion-cache nil
  "Caches completions.  Keys are list of the form '(MTYPE...)")
(make-variable-buffer-local 'greql--completion-cache)

(defun greql-completion-list (mtypes)
  "Return a completion list of all MTYPES (:meta values)."
  (when tg-schema-alist
    (unless greql--completion-cache
      (setq greql--completion-cache (make-hash-table :test 'equal)))
    (let ((completions (gethash mtypes greql--completion-cache)))
      (if completions
          completions
        (dolist (elem tg-schema-alist)
          (when (or (null mtypes) (member (plist-get elem :meta) mtypes))
            (setq completions (cons (list (plist-get elem :qname)
                                          (concat "  " (tg-eldoc-vertex-or-edge elem)))
                                    completions))))
        (puthash mtypes completions greql--completion-cache))
      completions)))

(defun greql-attribute-completion-list (al)
  "Format the attribute list AL for completion.
AL is in the return format of `tg-all-attributes-multi'."
  (when al
    (let ((cl (mapcar
               (lambda (plst)
                 (list (plist-get plst :name)
                       (concat " : " (plist-get plst :domain)
                               " (" (tg-unique-name (plist-get plst :owner) 'unique) ")")))
               al)))
      cl)))

(defun greql-completion-sort (c1 c2)
  (string-lessp (car c1) (car c2)))

(defvar greql--last-completion-list nil)
(make-variable-buffer-local 'greql--last-completion-list)

(defun greql-complete-1 (completion-list &optional backward-regexp)
  (let* ((window (get-buffer-window "*Completions*" 0))
         (beg (save-excursion
                (+ 1 (or (re-search-backward
                          (or backward-regexp "[^[:word:]._]")
                          nil t) 0))))
         (word (buffer-substring-no-properties beg (point)))
         (compl (try-completion word completion-list)))
    (if (and (eq last-command this-command)
             (equal greql--last-completion-list completion-list)
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
        ;; Do nothing here.
        (message "No completion possible."))
       ((stringp compl)
        (if (string= word compl)
            ;; Show completion buffer
            (let ((list (all-completions word completion-list)))
              (with-output-to-temp-buffer "*Completions*"
                (display-completion-list
                 (sort (remove-if-not
                        (lambda (elem)
                          (if (stringp elem)
                              (member elem list)
                            (member (car elem) list)))
                        completion-list)
                       'greql-completion-sort)
                 word)))
          ;; Complete
          (delete-region beg (point))
          (insert compl)
          ;; close completion buffer if there's one
          (let ((win (get-buffer-window "*Completions*" 0)))
            (if win (quit-window nil win)))))
       (t (message "That's the only possible completion.")))))
  (setq greql--last-completion-list completion-list))

(defun greql-completion-list-at-point (&optional arg)
  "Return a list of possible completions at point.
When ARG is given, complete more restrictive.  For example, if
there's a variable that is bound to either a Foo or a Bar, then
show only those attributes, which are valid for both Foo and Bar
elements."
  (cond
   ;; Complete vertex classes
   ((or (greql-vertex-set-expression-p)
        (greql-vsubgraph-expression-p)
        (greql-start-or-goal-restriction-p nil))
    (nconc (greql-import-completion-list '(VertexClass))
           (greql-completion-list '(VertexClass))))
   ;; Complete edge classes
   ((or (greql-edge-set-expression-p)
        (greql-esubgraph-expression-p)
        (greql-edge-restriction-p nil))
    (nconc (greql-import-completion-list '(EdgeClass))
           (greql-completion-list '(EdgeClass))))
   ;; Complete any classes
   ((greql-import-p)
    (nconc (greql-import-completion-list '(VertexClass EdgeClass))
           (greql-completion-list '(VertexClass EdgeClass))))
   ;; complete attributes
   ((greql-variable-p)
    (greql-attribute-completion-list
     (let ((vartypes (greql-variable-types-at-point)))
       (tg-all-attributes-multi (car vartypes) (cadr vartypes) arg))))
   ;; complete keywords / functions
   (t
    (cl-flet ((format-entry (fun)
			    (let* ((name (plist-get fun :name))
				   (i (- 79 (length name))))
			      (list name
				    (format (concat "% " (number-to-string i) "."
						    (number-to-string (- i 2)) "s")
					    (plist-get fun :description))))))
      (append
       (mapcar 'format-entry
	       greql-keywords)
       (mapcar 'format-entry
	       greql-functions))))))

(defun greql-complete (arg)
  "Complete word at point intelligently.
When ARG is given, complete more restrictive.  For example, if
there's a variable that is bound to either a Foo or a Bar, then
show only those attributes, which are valid for both Foo and Bar
elements."
  (interactive "P")
  (greql-complete-1 (greql-completion-list-at-point arg)
                    (when (greql-variable-p) "[.]")))

(defvar greql--indent-regexp
  "\\(?:\\_<\\(?:exists!?\\|forall\\|from\\)\\_>\\|(\\)")

(defvar greql--deindent-regexp
  "\\(?:)\\|\\_<end\\_>\\)")

(defun greql-calculate-indent ()
  "Calculate the indentation level of the current line."
  (let ((i 0) (d 0)
        (bound (save-excursion
                 (forward-line -1)
                 (point)))
        (prev-line-indent
         (save-excursion
           (forward-line -1)
           (if (looking-at "^\\([[:space:]]*\\)[^[:space:]]")
               (- (match-end 1) (match-beginning 1))
             0)))
        (regexp "[[:space:]]*\\(?:)\\|with\\|end\\|report\\(?:\\|List\\|Map\\|Set\\)\\)"))
    (save-excursion
      (forward-line 0)
      (when (looking-at regexp)
        (setq i (1- i)))
      (while (re-search-backward greql--indent-regexp bound t)
        (setq i (1+ i))))
    (save-excursion
      (forward-line 0)
      (while (re-search-backward greql--deindent-regexp bound t)
        (setq d (1+ d)))
      (goto-char bound)
      (when (looking-at regexp)
        (setq d (1- d))))
    (+ (/ prev-line-indent tab-width) (- i d))))

(defun greql-indent-line ()
  "Indent the line according to the previous line and syntactic elements."
  (let ((d (greql-calculate-indent))
        (col (- (point) (line-beginning-position)))
        spaces)
    (forward-line 0)
    (re-search-forward "^[[:space:]]*" (line-end-position) t)
    (setq spaces (- (match-end 0) (match-beginning 0)))
    (replace-match "")
    (dotimes (i (* tab-width d))
      (insert " "))
    (when (> col spaces)
      (forward-char (- col spaces)))
    (when (looking-back "^[[:space:]]+")
      (skip-chars-forward " \t"))))

(defun greql-format ()
  "Formats the current buffer or marked region."
  (interactive)
  (save-excursion
    (let ((beg (if (use-region-p) (region-beginning) (point-min)))
          (end (if (use-region-p) (region-end) (point-max)))
          (line-break-regexp "\\_<\\(?:from\\|with\\|report\\(?:\\|Set\\|Map\\)\\|and\\|end\\)\\_>"))
      (goto-char beg)
      (while (search-forward "\n" end t)
        (replace-match " " nil t))
      (goto-char (1+ beg))
      (while (re-search-forward line-break-regexp end t)
        (goto-char (match-beginning 0))
        (when (looking-back "[[:space:]]")
          (replace-match "" nil t))
        (insert "\n")
        (forward-char 1))
      (goto-char beg)
      (while (< (point) end)
        (greql-indent-line)
        (forward-line 1))
      (greql-indent-line)
      (delete-trailing-whitespace))))

(defvar greql-process nil
  "Network process to the GreqlEvalServer.")
(make-variable-buffer-local 'greql-process)

(defvar greql-server-port 10101
  "The port where the GreqlServer listenes for connections.")

(defun greql-execute (arg)
  "Execute the query in the current buffer on `greql-graph'.
If a region is active, use only that as query.
If a prefix ARG is given, also generate a DOT file containing the
elements calculated by the query plus all edges running between
vertices in the query result."
  (interactive "P")
  (let ((buffer (get-buffer-create greql-evaluation-buffer))
        (queryfile (if (use-region-p)
                       (let ((f (make-temp-file "greql-query"))
                             (str (buffer-substring-no-properties
                                   (region-beginning) (region-end))))
                         (with-current-buffer (find-file-noselect f)
                           (insert str)
                           (save-buffer)
                           (kill-buffer))
                         f)
                     (save-buffer)
                     (expand-file-name (buffer-file-name)))))
    (with-current-buffer buffer (erase-buffer))
    (when (or (not greql-process) (not (eq (process-status greql-process) 'open)))
      (setq greql-process (make-network-process
                           :name "GreqlEvalServer Connection"
                           ;; :buffer buffer
                           :host "localhost"
                           :service greql-server-port
                           :filter 'greql-filter-result)))
    (process-send-string greql-process (concat "g:" (expand-file-name greql-graph) "\n"))
    (process-send-string greql-process (concat (if arg "d:" "q:") queryfile "\n"))))

(defvar greql--error-overlay nil)
(make-variable-buffer-local 'greql--error-overlay)

(defun greql-filter-result (proc str)
  (let ((buf (get-buffer-create greql-evaluation-buffer))
        pos len)
    (with-current-buffer buf
      (goto-char (point-max))
      (save-excursion (insert str))
      (when (re-search-forward
             "Exception:.* at position (\\([[:digit:]]+\\),[[:space:]]*\\([[:digit:]]+\\))"
             nil t)
        (setq pos (string-to-number (match-string 1))
              len (string-to-number (match-string 2)))))
    (display-buffer buf)
    (message "(%s, %s)" pos len)
    (if (and pos len)
        (if (overlayp greql--error-overlay)
            (move-overlay greql--error-overlay pos (+ pos len))
          (setq greql--error-overlay (make-overlay pos (+ pos len) nil t))
          (overlay-put greql--error-overlay 'face font-lock-warning-face))
      (when greql--error-overlay
        (delete-overlay greql--error-overlay))
      (setq greql--error-overlay nil))))

(defun greql-vertex-set-expression-p ()
  (looking-back "V{[[:word:]._,^ ]*"))

(defun greql-vsubgraph-expression-p ()
  (looking-back "vSubgraph{[[:word:]._,^ ]*"))

(defun greql-esubgraph-expression-p ()
  (looking-back "eSubgraph{[[:word:]._,^ ]*"))

(defun greql-start-or-goal-restriction-p (after-at)
  (and
   (not (greql-vsubgraph-expression-p))
   (not (greql-esubgraph-expression-p))
   (looking-back (concat "[^-VE>]{[[:word:]._,^ ]*"
                         (if after-at
                             "@[[:word:]._,()^ ]*"
                           "")))))

(defun greql-edge-set-expression-p ()
  (looking-back "E{[[:word:]._,^ ]*"))

(defun greql-edge-restriction-p (after-at)
  (looking-back
   (concat "\\(<--\\|-->\\|<>--\\|--<>\\)[[:space:]]*{[[:word:]._,^ ]*"
           (if after-at
               "@[[:word:]._,()^ ]*"
             ""))))

(defun greql-variable-p ()
  (and (looking-back "[[:word:]_]\\.[[:word:]_]*?")
       (not (looking-back "import .*"))
       (not (looking-back "{[[:word:]_.]*?"))))

(defun greql-import-p ()
  (looking-back "import[[:space:]]+[[:word:]._]*"))

(defun greql-variable-types-at-point ()
  "Return the possible types the variable at point might have.
For example, that could be something like
  (VertexClass (\"Type1\" \"Type2\")),
for some variable declared as
  x : V{Type1, Type2}."
  (save-excursion
    (search-backward "." nil t 1)
    (let ((end (point))
          var)
      (re-search-backward "[[:space:],()@]" nil t 1)
      (setq var (buffer-substring-no-properties (+ 1 (point)) end))
      (re-search-backward
       (concat "\\_<"
               var
               "\\_>[^:{}]*:[[:space:]]*\\([VE]\\){\\([^}]+\\)}") nil t 1)
      (when (and (match-beginning 1) (match-end 1))
        (let* ((mtype-match (buffer-substring-no-properties (match-beginning 1)
                                                            (match-end 1)))
               (mtype (cond
                       ((or (not mtype-match) (string= mtype-match "E")) 'EdgeClass)
                       ((string= mtype-match "V") 'VertexClass)
                       (t (error "Not match!"))))
               (types (replace-regexp-in-string
                       "[[:space:]]+" ""
                       (buffer-substring-no-properties (match-beginning 2)
                                                       (match-end 2)))))
          (if (string= types "")
              (setq types nil)
            (setq types (split-string types ",")))
          (list mtype types))))))

(defun greql-kill-region-as-java-string (beg end)
  "Put the marked region (BEG and END) as java string on the `kill-ring'."
  (interactive "r")
  (let ((text (buffer-substring-no-properties beg end)))
    (with-temp-buffer
      (insert text)
      (replace-string "\"" "\\\"" nil (point-min) (point-max))
      (goto-char (point-min))
      (insert "\"")
      (while (re-search-forward "\n" nil t)
        (replace-match " \"\n+ \""))
      (insert "\"")
      (kill-region (point-min) (point-max)))))

;;** Yasnippets

(when (featurep 'yasnippet)
    (yas/define-snippets
     'greql-mode
     '(("fr"  "from $1\nreport$0\nend"          "from ... report ... end")
       ("fwr" "from $1\nwith $2\nreport$0\nend" "from ... with ... report ... end")
       ("ex"  "exists $1 @ $0"                  "exists ... @ ...")
       ("fa"  "forall $1 @ $0"                  "forall ... @ ...")
       ("vs"  "V{$1}$0"                         "V{...}")
       ("es"  "E{$1}$0"                         "E{...}")
       ("e+"  "-->{$1}$0"                       "-->{...}")
       ("e-"  "<--{$1}$0"                       "<--{...}")
       ("e="  "<->{$1}$0"                       "<->{...}")
       ("a+"  "<>--{$1}$0"                      "<>--{...}")
       ("a-"  "--<>{$1}$0"                      "--<>{...}"))))

;;** Auto-Complete support

;; copied from bbdb
(defsubst greql-string-trim (string)
  "Remove leading and trailing whitespace from STRING.
Also remove all properties from string."
  (if (string-match "\\`[ \t\n]+" string)
      (setq string (substring string (match-end 0))))
  (if (string-match "[ \t\n]+\\'" string)
      (setq string (substring string 0 (match-beginning 0))))
  ;; This is not ideologically blasphemous.  It is a bad function to
  ;; use on regions of a buffer, but since this is our string, we can
  ;; do whatever we want with it. --Colin
  (set-text-properties 0 (length string) nil string)
  string)

(when (featurep 'auto-complete)
  (defun greql-ac-documentation (symbol)
    (cond
     ((stringp symbol)
      (greql-string-trim symbol))
     ((and (listp symbol) (= (length symbol) 1))
      (greql-ac-documentation (car symbol)))
     ((and (listp symbol) (> (leyngth symbol) 1))
      (greql-ac-documentation (cadr symbol)))
     ((symbolp symbol)
      (symbol-name symbol))
     (t symbol)))

  (ac-define-source greql
    '((candidates . greql-completion-list-at-point)
      (document   . greql-ac-documentation)
      (symbol     . "GReQL")
      (requires   . 1)))

  (add-to-list 'ac-modes 'greql-mode)

  (defun greql-activate-auto-complete ()
    (add-to-list 'ac-sources 'ac-source-greql)
    ;; Don't show completions for normal words and file names.
    (setq ac-sources (delq 'ac-source-filename ac-sources))
    (setq ac-sources (delq 'ac-source-words-in-same-mode-buffers ac-sources))
    ;; Assure the yasnippets are also listed when available
    (when (featurep 'yasnippet)
      (add-to-list 'ac-sources 'ac-source-yasnippet)))

  (add-hook 'greql-mode-hook 'greql-activate-auto-complete))

;;** Eldoc & GReQL Doc

(defvar greql--last-thing "")
(make-variable-buffer-local 'greql--last-thing)
(defvar greql--last-doc "")
(make-variable-buffer-local 'greql--last-doc)

(defvar greql-doc-buffer "*GReQL Documentation*"
  "The name of the GReQL documentation buffer.")

(defun greql-doc-next (n)
  (interactive "p")
  (forward-line 1)
  (re-search-forward "^Function `" nil t n)
  (forward-line 0))

(defun greql-doc-previous (n)
  (interactive "p")
  (forward-line -1)
  (re-search-backward "^Function `" nil t n)
  (forward-line 0))

(define-derived-mode greql-doc-mode nil "GReQLDoc"
  "Mode used in *GReQL Documentation* buffers."
  :group 'greql
  (setq buffer-read-only t)
  (set-fill-column 72)
  (setq font-lock-defaults '(greql-fontlock-keywords-doc))
  (font-lock-mode 1)

  (progn
    (define-key greql-doc-mode-map (kbd "q") 'bury-buffer)

    (define-key greql-doc-mode-map (kbd "f") 'forward-char)
    (define-key greql-doc-mode-map (kbd "b") 'backward-char)
    (define-key greql-doc-mode-map (kbd "n") 'next-line)
    (define-key greql-doc-mode-map (kbd "p") 'previous-line)

    (define-key greql-doc-mode-map (kbd "SPC") 'greql-doc-next)
    (define-key greql-doc-mode-map (kbd "DEL") 'greql-doc-previous)))

(defvar greql-fontlock-keywords-doc
  (let ((regex (regexp-opt (mapcar (lambda (elem) (plist-get elem :name))
                                   greql-functions) t)))
    (list (list (concat "\\(?:[`]?" regex "['(]\\)")
                1 font-lock-function-name-face)
          (list "^\\(Function\\)[[:space:]]+`"
                1 (quote 'bold))
          (list "^\\([=]+\\)$"
                1 (quote 'bold)))))

(defun greql--show-function-documentation (&optional func-name)
  (set-buffer (get-buffer-create greql-doc-buffer))
  (let ((inhibit-read-only t))
    (erase-buffer)
    (call-process "java" nil
                  (get-buffer-create greql-doc-buffer)
                  nil
                  "-cp" greql-jgralab-jar-file
                  "de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary"
                  "-a")

    (goto-char (point-min))
    (greql-doc-mode)

    ;; Format the buffer.
    (while (re-search-forward "^===+$" nil t)
      (forward-line 3)
      (let ((beg (point)))
        (re-search-forward "^Signatures:$" nil t)
        (forward-line -1)
        (fill-region beg (point)))))
  (goto-char (point-min))

  (switch-to-buffer (get-buffer-create greql-doc-buffer))
  (when func-name
    (re-search-forward (concat  "^Function `" func-name "':") nil t)
    (forward-line 0)))

(defun greql-show-function-documentation (fun)
  (interactive
   (list (completing-read "Function: "
                          (mapcar (lambda (elem)
                                    (plist-get elem :name))
                                  greql-functions))))
  (greql--show-function-documentation fun))

(defun greql-show-documentation ()
  (interactive)
  (cond
   ((let ((fun (greql-function-p)))
      (when fun
        (greql-show-function-documentation (plist-get fun :name)))))
   (t (message "No docs avaliable for this element."))))

(defun greql-documentation-function ()
  (let ((thing (thing-at-point 'sexp)))
    (if (string= thing greql--last-thing)
        greql--last-doc
      (setq greql--last-thing thing)
      (setq greql--last-doc
            (cond
             ;; Functions
             ((let ((fun (greql-function-p)))
                (when fun
                  (concat (propertize (plist-get fun :name) 'face 'font-lock-function-name-face)
                          ": "
                          (plist-get fun :description)))))
             ;; Keywords
             ((let ((key (greql-keyword-p)))
                (when key
                  (concat (propertize (plist-get key :name) 'face 'font-lock-keyword-face)
                          ": "
                          (plist-get key :description)))))
             ((null tg-schema-alist)
              "Set a graph for eldoc features.")
             ;; document vertex classes
             ((and (or (greql-vertex-set-expression-p)
                       (greql-start-or-goal-restriction-p nil))
                   (save-excursion
                     (re-search-backward "[{ ,]" (line-beginning-position) t)
                     (when (looking-at "[{ ,]\\([[:alnum:]._]+\\)")
                       (let ((vc (tg-get-schema-element 'VertexClass (match-string-no-properties 1))))
                         (if vc
                             (tg-eldoc-vertex-or-edge vc)
                           "Name is neither qualified nor unique."))))))
             ;; document edge classes
             ((and (or (greql-edge-set-expression-p)
                       (greql-edge-restriction-p nil))
                   (save-excursion
                     (re-search-backward "[{ ,]" (line-beginning-position) t)
                     (when (looking-at "[{ ,]\\([[:alnum:]._]+\\)")
                       (let ((ec (tg-get-schema-element 'EdgeClass (match-string-no-properties 1))))
                         (if ec
                             (tg-eldoc-vertex-or-edge ec)
                           "Name is neither qualified nor unique."))))))
             ;; nothing to be done...
             (t ""))))))

;;** GReTL Minor Mode

(define-minor-mode gretl-minor-mode
  "Add some more features to GReQL mode to make it more usable
for editing GReTL transformations."
  ;; The initial value.
  :init-value nil
  ;; The indicator for the mode line.
  :lighter " GReTL"
  (let ((regex
	 (rx (or (or "<==" ":=" ";")
		 (and bow
		      (or "AddSourceGraph"
			  "AddSubClass" "AddSubClasses"
			  "AddSuperClass" "AddSuperClasses"
			  "Assert" "ExecuteTransformation"
			  "CopyDomain"
			  "CreateAbstractEdgeClass"
			  "CreateAbstractVertexClass"
			  "CreateAttribute" "CreateAttributes"
			  "CreateEdgeClass" "CreateEdges"
			  "CreateEnumDomain" "CreateListDomain"
			  "CreateMapDomain" "CreateRecordDomain"
			  "CreateSetDomain" "CreateSubgraph"
			  "CreateVertexClass" "CreateVertices"
			  "Delete" "Iteratively" "MatchReplace"
			  "MergeVertices" "PrintGraph"
			  "RedefineFromRole" "RedefineFromRoles"
			  "RedefineToRole" "RedefineToRoles"
			  "SetAttributes" "SetMultipleAttributes"
			  "SysOut" "SysErr" "transformation")
		      eow)))))
    (if gretl-minor-mode
	(progn
	  (message "Enabling GReTL support...")
	  (hi-lock-face-buffer regex 'bold))
      (message "Disabling GReTL support...")
      (hi-lock-unface-buffer regex))))

(provide 'greql-mode)

;;; greql-mode.el ends here
