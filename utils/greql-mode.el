;;; greql-mode.el --- Major mode for editing GReQL2 files with emacs

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

;; Major mode for editing GReQL2 files with Emacs and executing queries.

;;; Version:
;; <2009-10-28 Wed 08:58>

;;; Code:

(defvar greql-keywords
  '("E" "V" "as" "bag" "eSubgraph" "end" "exists!" "exists" "forall"
    "from" "in" "let" "list" "path" "pathSystem" "rec" "report"
    "reportBag" "reportSet" "reportMap" "set" "store" "tup" "using"
    "vSubgraph" "where" "with" "thisEdge" "thisVertex" "map")
  "GReQL keywords that should be completed and highlighted.")
(put 'greql-keywords 'risky-local-variable-p t)

(defvar greql-jgralab-jar-file
  "/home/horn/uni/repos/jgralab/build/jar/jgralab.jar")

(defun greql-functions ()
  "Returns a list of all available GReQL function lists."
  (with-temp-buffer
    (call-process "java" nil
                  (current-buffer)
                  nil
                  "-cp" greql-jgralab-jar-file
                  "de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary")
    (goto-char (point-min))
    (let (list)
      (while (re-search-forward "\\([[:alpha:]][[:alnum:]]*\\)$" nil t)
        (setq list (cons (match-string 1) list)))
      list)))

(defvar greql-functions (greql-functions)
  "GReQL functions that should be completed and highlighted.")
(put 'greql-functions 'risky-local-variable-p t)

(dolist (ext '("\\.greqlquery$" "\\.grq$" "\\.greql$"))
  (add-to-list 'auto-mode-alist (cons ext 'greql-mode)))

(defvar greql-fontlock-keywords-1
  `(
    ;; Highlight function names
    ,(cons (concat "\\<" (regexp-opt greql-functions t) "\\>")
           font-lock-function-name-face)
    ;; Highlight strings
    ,(list "\".*?\"" 0 font-lock-string-face t)
    ;; Highlight one-line comments
    ,(list "//.*$" 0 font-lock-comment-face t)))

(defvar greql-fontlock-keywords-2
  (append greql-fontlock-keywords-1
          (list (concat "\\<" (regexp-opt greql-keywords t) "\\>"))))

(defvar greql-fontlock-keywords-3 greql-fontlock-keywords-2)
(make-variable-buffer-local 'greql-fontlock-keywords-3)

(defun greql-set-fontlock-keywords-3 ()
  (setq greql-fontlock-keywords-3
        (append greql-fontlock-keywords-2
                (list (list
                       (concat "{\\(\\([\s^,!]*"
                               (regexp-opt
                                (let (lst)
                                  (dolist (i greql-schema-alist)
                                    (when (or (eq (car i) 'EdgeClass)
                                              (eq (car i) 'VertexClass))
                                      (setq lst (cons (second i) lst))))
                                  lst) t)
                               "\\)+\\)}")
                       1 font-lock-type-face))))
  (setq font-lock-keywords greql-fontlock-keywords-3)
  ;; TODO: Redisplay seems not to suffice
  (redisplay t))

(defvar greql-tab-width 2
  "Distance between tab stops (for display of tab characters), in
columns.")

(defvar greql-program "~/repos/utils/greqleval"
  "The program to execute GReQL queries.")

(defvar greql-buffer "*GReQL*"
  "Name of the GReQL status buffer.")

(define-derived-mode greql-mode text-mode "GReQL"
  "A major mode for GReQL2."
  ;; Comments
  (set (make-local-variable 'comment-start)      "/*")
  (set (make-local-variable 'comment-start-skip) "\\(//+\\|/\\*+\\)\\s *")
  (set (make-local-variable 'comment-end)        " */")

  ;; Keywords
  (setq font-lock-defaults
        '((greql-fontlock-keywords-1
           greql-fontlock-keywords-2
           greql-fontlock-keywords-3)))

  (setq tab-width greql-tab-width)
  (set (make-local-variable 'indent-line-function) 'greql-indent-line)

  ;; List of functions to be run when mode is activated
  (define-key greql-mode-map (kbd "M-TAB")   'greql-complete)
  (define-key greql-mode-map (kbd "C-c C-v") 'greql-complete-vertexclass)
  (define-key greql-mode-map (kbd "C-c C-e") 'greql-complete-edgeclass)
  (define-key greql-mode-map (kbd "C-c C-d") 'greql-complete-domain)
  (define-key greql-mode-map (kbd "C-c C-s") 'greql-set-graph)
  (define-key greql-mode-map (kbd "C-c C-p") 'greql-set-extra-classpath)
  (define-key greql-mode-map (kbd "C-c C-c") 'greql-execute))

(defvar greql-graph nil
  "The graph which is used to extract schema information on which
queries are evaluated.  Set it with `greql-set-graph'.")
(make-variable-buffer-local 'greql-graph)

(defvar greql-extra-classpath nil
  "Extra classpath elements as string.")
(make-variable-buffer-local 'greql-extra-classpath)

(defvar greql-schema-alist nil)
(make-variable-buffer-local 'greql-schema-alist)

(defun greql-initialize-schema ()
  (when (and greql-graph (not greql-schema-alist))
    (greql-set-graph greql-graph)
    (greql-set-fontlock-keywords-3)))

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
                                   greql-schema-alist)))

  (greql-set-fontlock-keywords-3))

(defun greql-set-extra-classpath (file-or-dir)
  (interactive "FExtra classpath: ")
  (setq greql-extra-classpath file-or-dir))

(defun greql-parse-schema ()
  "Parse `greql-graph' and extract schema information into
`greql-schema-alist'."
  (interactive) ;; TODO: Remove interactive spec...
  (goto-char (point-min))
  (let ((current-package "")
        schema-alist
        finished)
    (while (not finished)
      (cond
       ;; Packages
       ((looking-at "^Package\s+\\([[:alnum:]._]+\\)")
        (let ((match (match-string 1)))
          (setq current-package (if (string-match "^\s*$" match)
                                    ""
                                  (concat  (match-string 1) "."))))
        ;;(message "Found pkg '%s'" current-package)
        )
       ;; GraphClass
       ((looking-at "^GraphClass\s+\\([[:alnum:]._]+\\)\s*\\(?:{\\([^}]*\\)}\\)?")
        ;;(message "GraphClass %s" (match-string 1))
        )
       ;; VertexClass
       ((looking-at "^\\(?:abstract\\)?\s*VertexClass\s+\\([[:alnum:]._]+\\)\s*\\(?::\\([^{;]+\\)\\)?\s*\\(?:{\\([^}]*\\)}\\)?")
        (setq schema-alist
              (cons (list 'VertexClass
                          (concat current-package (match-string 1)) 
                          (greql-parse-superclasses (match-string 2) current-package)
                          (greql-parse-attributes (match-string 3)))
                    schema-alist)))
       ;; EdgeClasses
       ((looking-at (concat "^\\(?:abstract\s+\\)?"
                            "\\(?:Edge\\|Aggregation\\|Composition\\)Class\s+"
                            "\\([[:alnum:]._]+\\)\s*"      ;; Name
                            "\\(?::\\([[:alnum:]._ ]+\\)\\)?\s+"    ;; Supertypes
                            "from [^{;]+"                       ;; skip from/to, roles, multis
                            "\\(?:{\\([^}]*\\)}\\)?"       ;; Attributes
                            ))
        (setq schema-alist
              (cons (list 'EdgeClass
                          (concat current-package (match-string 1)) 
                          (greql-parse-superclasses (match-string 2) current-package)
                          (greql-parse-attributes (match-string 3)))
                    schema-alist)))
       ;; End of schema (part)
       ((or (= (point) (point-max)))
        (looking-at "Graph[[:space:]]+")
        (setq finished t)))
      (forward-line))
    schema-alist))

(defun greql-parse-superclasses (str current-package)
  "Given a string \"Foo, Bar, Baz\" it returns (\"Foo\" \"Bar\"
\"Baz\")"
  (when str
    (setq str (replace-regexp-in-string "[[:space:]]+" "" str))
    (save-match-data
      (mapcar
       (lambda (class) (concat current-package class))
       (split-string str "[,]+")))))

(defun greql-parse-attributes (str)
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

(defun greql-completion-list (&optional types)
  (when greql-schema-alist
    (let (completions)
      (dolist (line greql-schema-alist)
        (when (or (null types) (member (car line) types))
          (let ((elem (cadr line)))
            (setq completions (cons elem completions)))))
      completions)))

(defun greql-complete-1 (completion-list &optional backward-regexp)
  (let* ((window (get-buffer-window "*Completions*" 0))
         (beg (save-excursion
                (+ 1 (or (re-search-backward
                          (or backward-regexp "[^[:word:]._]")
                          nil t) 0))))
         (word (buffer-substring-no-properties beg (point)))
         (compl (try-completion word
                                completion-list)))
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
                                         completion-list)))
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
  (greql-initialize-schema)
  (cond
   ;; Complete vertex classes
   ((or (greql-vertex-set-expression-p)
        (greql-start-or-goal-restriction-p))
    (greql-complete-vertexclass))
   ;; Complete edge classes
   ((or (greql-edge-set-expression-p)
        (greql-edge-restriction-p))
    (greql-complete-edgeclass))
   ;; complete attributes
   ((greql-variable-p)
    (let* ((vartypes (greql-variable-types))
           (attrs (greql-attributes vartypes)))
      (greql-complete-1 attrs "[.]")))
   ;; complete keywords
   (t (greql-complete-keyword-or-function))))

(defun greql-complete-vertexclass ()
  (interactive)
  (greql-complete-1 (greql-completion-list '(VertexClass))))

(defun greql-complete-edgeclass ()
  (interactive)
  (greql-complete-1 (greql-completion-list '(EdgeClass))))

(defun greql-complete-domain ()
  (interactive)
  (greql-complete-1 (greql-completion-list
                     '(EnumDomain RecordDomain ListDomain SetDomain BagDomain))))

(defun greql-complete-keyword-or-function ()
  (interactive)
  (greql-complete-1 (greql-completion-list '(keyword funlib))))

(defvar greql-result-file nil)

(defun greql-execute ()
  "Execute the query in the current buffer on `greql-graph'."
  (interactive)
  (let ((buffer (get-buffer-create greql-buffer))
        (evalstr (buffer-substring-no-properties (point-min) (point-max))))
    (setq greql-result-file (make-temp-file "greql-result" nil ".html"))
    (with-current-buffer buffer (erase-buffer))
    (let ((proc (start-process "GReQL process" buffer
                               greql-program
                               (if greql-extra-classpath
                                    "--extra-cp"
                                 "")
                               (if greql-extra-classpath
                                    greql-extra-classpath
                                 "")
                               evalstr
                               (expand-file-name greql-graph))))
      (set-process-sentinel proc 'greql-display-result))
    (display-buffer buffer)))

(defun greql-display-result (proc change)
  (select-window (get-buffer-window (get-buffer-create greql-buffer))))

(defun greql-vertex-set-expression-p ()
  (looking-back "V{[[:word:]._,^ ]*"))

(defun greql-start-or-goal-restriction-p ()
  (looking-back "[^-VE>]{[[:word:]._,^ ]*"))

(defun greql-edge-set-expression-p ()
  (looking-back "E{[[:word:]._,^ ]*"))

(defun greql-edge-restriction-p ()
  (looking-back "\\(<--\\|-->\\|<>--\\|--<>\\)[ ]*{[[:word:]._,^ ]*"))

(defun greql-variable-p ()
  (looking-back "[^{][[:word:]]+[.][[:word:]]*"))

(defun greql-variable-types ()
  "Return something like (VertexClass (Type1 Type2 Type3))."
  (save-excursion
    (search-backward "." nil t 1)
    (let ((end (point))
          var)
      (re-search-backward "[[:space:],]" nil t 1)
      (setq var (buffer-substring-no-properties (+ 1 (point)) end))
      (re-search-backward
       (concat var "[[:space:],]*[[:alnum:][:space:]]*:[[:space:]]*\\([VE]\\){\\(.*\\)}") nil t 1)
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
        (list mtype (split-string types "[,]"))))))

(defun greql-attributes (typelist)
  (greql-attributes-1 (car typelist) (cadr typelist)))

(defun greql-find-schema-line (mtype type)
  "Get the line/list of `greql-schema-alist' that corresponds to
MTYPE TYPE."
  (dolist (line greql-schema-alist)
    (when (and (eq mtype (car line))
               (string= type (second line)))
      (return line))))

(defun greql-all-attributes (mtype type)
  "Returns a list of all attribute names of the MTYPE TYPE (and
its supertypes)."
  (let ((line (greql-find-schema-line mtype type)))
    (apply 'append
           (fourth line)
           (mapcar (lambda (supertype)
                     (greql-all-attributes mtype supertype))
                   (third line)))))

(defun greql-attributes-1 (mtype types)
  "Returns a list of all attribute names that are defined in all
MTYPEs TYPES."
  (let ((attr-list (mapcar
                    (lambda (type)
                      (greql-all-attributes mtype type))
                    types)))
    (if (= (length attr-list) 1)
        (car attr-list)
      (apply 'intersection
             attr-list))))

(defun greql-indent-keywords ()
  (remove-if (lambda (s)
               (string-match "^\\(E\\|V\\|using\\)" s))
             greql-keywords))

(defparameter greql-indent-regexp
  (concat "\\([()]\\|\\_<"
          (regexp-opt (greql-indent-keywords))
          "\\_>\\)"))

(defun greql-indent-line ()
  (interactive)
  (save-excursion
    (let* ((col (save-excursion
                  (beginning-of-line)
                  (let ((run t))
                    (while (and run
                                (not (and
                                      (or (re-search-backward
                                           greql-indent-regexp nil t)
                                          (setq run nil))
                                      (not (save-match-data
                                             (looking-back "^.*//.*"))))))))
                  (current-indentation)))
           (key (and col (match-string 0))))
      (when key
        (cond
         ((string-match (regexp-opt '(")" "end")) key)
          (indent-line-to (- col tab-width)))
         ((string-match
           "[ ]*\\([)]\\|\\(with\\|report\\|end\\(Set\\|Map\\Bag\\)?\\)\\_>\\)"
           (buffer-substring-no-properties
            (line-beginning-position)
            (line-end-position)))
          (indent-line-to col))
         (t (indent-line-to (+ col tab-width))))))))

(defun greql-kill-region-as-java-string (beg end)
  "Puts the marked region as java string on the kill-ring."
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

(provide 'greql-mode)

;;; greql-mode.el ends here
