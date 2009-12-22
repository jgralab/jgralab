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
;; $Revision$

;;; TODO:
;; - Implement handling of imports in completion (DONE) and highlighting (still
;;   not done)

;;* Code

;;** Main

;; TG mode contains the schema parsing stuff.
(require 'tg-mode)

(defparameter greql-keywords
  (sort '("E" "V" "as" "bag" "eSubgraph" "end" "exists!" "exists" "forall"
          "from" "in" "let" "list" "path" "pathSystem" "rec" "report"
          "reportBag" "reportSet" "reportMap" "set" "store" "tup" "using"
          "vSubgraph" "where" "with" "thisEdge" "thisVertex" "map" "import"
          "true" "false" "null")
        'string-lessp)
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
      (while (re-search-forward "^\\([[:alpha:]][[:alnum:]]*\\)$" nil t)
        (setq list (cons (match-string 1) list)))
      (nreverse list))))

(defparameter greql-functions (greql-functions)
  "GReQL functions that should be completed and highlighted.")
(put 'greql-functions 'risky-local-variable-p t)

(dolist (ext '("\\.greqlquery$" "\\.grq$" "\\.greql$"))
  (add-to-list 'auto-mode-alist (cons ext 'greql-mode)))

(defparameter greql-fontlock-keywords-1
  `(
    ;; Highlight function names
    ,(list (concat "[^.]" (regexp-opt greql-functions 'words) "[^.]")
           1 font-lock-function-name-face)
    ;; Highlight strings
    ,(list "\".*?\"" 0 font-lock-string-face t)
    ;; Highlight one-line comments
    ,(list "//.*$" 0 font-lock-comment-face t)))

(defparameter greql-fontlock-keywords-2
  (append greql-fontlock-keywords-1
          (list (concat "\\<" (regexp-opt greql-keywords t) "\\>"))))

(defvar greql-fontlock-keywords-3 nil)
(make-variable-buffer-local 'greql-fontlock-keywords-3)

(defun greql-set-fontlock-keywords-3 ()
  (setq greql-fontlock-keywords-3
        (append greql-fontlock-keywords-2
                (list (list
                       (concat "{\\(\\([[:space:]^,!]*"
                               (regexp-opt
                                (mapcar
                                 (lambda (elem) (car elem))
                                 (append (greql-import-completion-list '(EdgeClass VertexClass))
                                         (greql-completion-list '(EdgeClass VertexClass))))
                                t)
                               "\\)+\\)}")
                       1 font-lock-type-face))))
  ;; TODO: This should not be needed
  (set (make-local-variable 'font-lock-keywords) greql-fontlock-keywords-3)
  ;; TODO: Redisplay seems not to suffice
  (redisplay t))

(defvar greql-tab-width 2
  "Distance between tab stops (for display of tab characters), in
columns.")

(defvar greql-buffer "*GReQL*"
  "Name of the GReQL status buffer.")

(define-derived-mode greql-mode text-mode "GReQL"
  "A major mode for GReQL2."
  ;; Comments
  (set (make-local-variable 'comment-start)      "//")
  (set (make-local-variable 'comment-end)        "")

  ;; Keywords
  (greql-set-fontlock-keywords-3)
  (setq font-lock-defaults
        '((greql-fontlock-keywords-1
           greql-fontlock-keywords-2
           greql-fontlock-keywords-3)))
  ;; Update highlighting of Vertex- and EdgeClasses after saving
  (add-hook 'after-save-hook 'greql-set-fontlock-keywords-3 t t)

  (setq tab-width greql-tab-width)
  (set (make-local-variable 'indent-line-function) 'greql-indent-line)
  (set (make-local-variable 'eldoc-documentation-function)
       'greql-documentation-function)

  (greql-add-functions-and-keywords)

  (define-key greql-mode-map (kbd "M-TAB")   'greql-complete)
  (define-key greql-mode-map (kbd "C-c C-v") 'greql-complete-vertexclass)
  (define-key greql-mode-map (kbd "C-c C-e") 'greql-complete-edgeclass)
  (define-key greql-mode-map (kbd "C-c C-d") 'greql-complete-domain)
  (define-key greql-mode-map (kbd "C-c C-s") 'greql-set-graph)
  (define-key greql-mode-map (kbd "C-c C-c") 'greql-execute)
  (define-key greql-mode-map (kbd "C-c C-f") 'greql-format))

(defvar greql-graph nil
  "The graph which is used to extract schema information on which
queries are evaluated.  Set it with `greql-set-graph'.")
(make-variable-buffer-local 'greql-graph)

(defun greql-add-functions-and-keywords ()
  (dolist (key greql-keywords)
    (setq tg-schema-alist (cons (list :meta 'keyword :name key)
                                tg-schema-alist)))
  (dolist (fun greql-functions)
    (setq tg-schema-alist (cons (list :meta 'function :name fun)
                                tg-schema-alist))))

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
  ;; add keywords and functions, too
  (greql-add-functions-and-keywords)
  ;; Setup schema element font locking
  (greql-set-fontlock-keywords-3))

(defun greql-import-completion-list (mtypes)
  "Additional completions due to imports."
  (let ((comp-lst (greql-completion-list mtypes))
        lst)
    (save-excursion
      (while (re-search-backward "import[[:space:]]+\\([^;]+\\);" nil t)
        (unless (looking-back ".*//.*")
          (let* ((import (match-string-no-properties 1))
                 (regex (regexp-quote (substring import 0 (- (length import) 1)))))
            (if (string-match "\\*$" import)
                ;; A package import
                (setq
                 lst
                 (nconc lst
                        (delq nil
                              (mapcar
                               (lambda (elem)
                                 (if (string-match regex (car elem))
                                     (list (replace-regexp-in-string regex "" (car elem))
                                           (cadr elem))
                                   nil))
                               comp-lst))))
              ;; An element import
              (when (member import comp-lst)
                (setq lst (cons
                           (replace-regexp-in-string "\\([[:word:]._]+\\.\\)\\([^.]+\\)" "\\2" import)
                           lst))))))))
    lst))

(defun greql-completion-list (mtypes &optional key)
  "Return a completion list of all MTYPES (:meta values) of
KEY (:qname by default)."
  (when tg-schema-alist
    (let (completions)
      (dolist (elem tg-schema-alist)
        (when (or (null mtypes) (member (plist-get elem :meta) mtypes))
          (setq completions (cons (list (plist-get elem (or key :qname))
                                        (concat " (" (symbol-name (plist-get elem :meta)) ")"))
                                  completions))))
      completions)))

(defun greql-attribute-completion-list (al)
  "Formats the attribute list retrieved by
tg-all-attributes-multi for completion."
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

(defun greql-complete-1 (completion-list &optional backward-regexp)
  (let* ((window (get-buffer-window "*Completions*" 0))
         (beg (save-excursion
                (+ 1 (or (re-search-backward
                          (or backward-regexp "[^[:word:]._]")
                          nil t) 0))))
         (word (buffer-substring-no-properties beg (point)))
         (compl (try-completion word completion-list)))
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
        ;; Do nothing here.
        nil)
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
       (t (message "That's the only possible completion."))))))

(defun greql-complete ()
  "Complete word at point somehow intelligently."
  (interactive)
  (cond
   ;; Complete vertex classes
   ((or (greql-vertex-set-expression-p)
        (greql-start-or-goal-restriction-p))
    (greql-complete-vertexclass))
   ;; Complete edge classes
   ((or (greql-edge-set-expression-p)
        (greql-edge-restriction-p))
    (greql-complete-edgeclass))
   ;; Complete any classes
   ((greql-import-p)
    (greql-complete-anyclass))
   ;; complete attributes
   ((greql-variable-p)
    (greql-complete-attributes))
   ;; complete keywords / functions
   (t (greql-complete-keyword-or-function))))

(defun greql-complete-attributes ()
  (interactive)
  (let ((vartypes (greql-variable-types)))
    (when vartypes
      (let ((compl-list (greql-attribute-completion-list
                         (tg-all-attributes-multi (car vartypes) (cadr vartypes)))))
        (greql-complete-1 compl-list "[.]")))))

(defun greql-complete-anyclass ()
  (interactive)
  (let ((types '(EdgeClass VertexClass)))
    (greql-complete-1 (nconc (greql-import-completion-list types)
                             (greql-completion-list types)))))

(defun greql-complete-vertexclass ()
  (interactive)
  (greql-complete-1 (nconc (greql-import-completion-list '(VertexClass))
                           (greql-completion-list '(VertexClass)))))

(defun greql-complete-edgeclass ()
  (interactive)
  (greql-complete-1 (nconc (greql-import-completion-list '(EdgeClass))
                           (greql-completion-list '(EdgeClass)))))

(defun greql-complete-domain ()
  (interactive)
  (let ((types '(EnumDomain RecordDomain ListDomain SetDomain BagDomain MapDomain)))
    (greql-complete-1 (nconc (greql-import-completion-list types)
                             (greql-completion-list types)))))

(defun greql-complete-keyword-or-function ()
  (interactive)
  (greql-complete-1 (greql-completion-list '(keyword function) :name)))

(defparameter greql--indent-regexp
  "\\(?:\\<\\(?:exists!?\\|forall\\|from\\)\\>\\|(\\)")

(defparameter greql--deindent-regexp
  "\\(?:)\\|\\<end\\>\\)")

(defun greql-calculate-indent ()
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
        (regexp "[[:space:]]*\\(?:)\\|with\\|end\\|report\\(?:\\|Bag\\|Map\\|Set\\)\\)"))
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
          (line-break-regexp "\\<\\(?:from\\|with\\|report\\(?:\\|Set\\|Map\\)\\|and\\|end\\)\\>"))
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
      (delete-trailing-whitespace))))

(defvar greql-process nil
  "Network process to the GreqlEvalServer.")
(make-variable-buffer-local 'greql-process)

(defun greql-execute ()
  "Execute the query in the current buffer on `greql-graph'.
If a region is active, use only that as query."
  (interactive)
  (let ((buffer (get-buffer-create greql-buffer))
        (queryfile (if (use-region-p)
                       (let ((f (make-temp-file "greql-query"))
                             (str (buffer-substring-no-properties (region-beginning) (region-end))))
                         (with-current-buffer (find-file-noselect f)
                           (insert str)
                           (save-buffer))
                         f)
                     (save-buffer)
                     (expand-file-name (buffer-file-name)))))
    (with-current-buffer buffer (erase-buffer))
    (when (or (not greql-process) (not (eq (process-status greql-process) 'open)))
      (setq greql-process (make-network-process
                           :name "GreqlEvalServer Connection"
                           :buffer buffer
                           ;; TODO:  This should be customizable
                           :host "localhost"
                           :service 10101
                           :sentinel 'greql-display-result)))
    (process-send-string greql-process (concat "g:" (expand-file-name greql-graph) "\n"))
    (process-send-string greql-process (concat "q:" queryfile "\n"))
    (display-buffer buffer)))

(defun greql-display-result (proc change)
  (display-buffer (get-buffer-create greql-buffer)))

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

(defun greql-import-p ()
  (looking-back "import[[:space:]]+[[:word:]._]*"))

(defun greql-variable-types ()
  "Return something like (VertexClass (\"Type1\" \"Type2\")),
for some variable declared as

  x : V{Type1, Type2}"
  (save-excursion
    (search-backward "." nil t 1)
    (let ((end (point))
          var)
      (re-search-backward "[[:space:],]" nil t 1)
      (setq var (buffer-substring-no-properties (+ 1 (point)) end))
      (re-search-backward
       (concat var "[[:space:],]*[[:alnum:][:space:]]*:[[:space:]]*\\([VE]\\){\\(.*\\)}") nil t 1)
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

;;** Eldoc

(defvar greql--last-thing "")
(make-variable-buffer-local 'greql--last-thing)
(defvar greql--last-doc "")
(make-variable-buffer-local 'greql--last-doc)

(defun greql-documentation-function ()
  (let ((thing (thing-at-point 'sexp)))
    (if (string= thing greql--last-thing)
        greql--last-doc
      (setq greql--last-thing thing)
      (setq greql--last-doc
            (cond
             ((null tg-schema-alist)
              "Set a graph for eldoc features.")
             ;; document vertex classes
             ((or (greql-vertex-set-expression-p)
                  (greql-start-or-goal-restriction-p))
              (save-excursion
                (re-search-backward "[{ ,]" (line-beginning-position) t)
                (when (looking-at "[{ ,]\\([[:alnum:]._]+\\)")
                  (tg-eldoc-vertex-or-edge (tg-get-schema-element 'VertexClass
                                                                  (match-string-no-properties 1))))))
             ;; document edge classes
             ((or (greql-edge-set-expression-p)
                  (greql-edge-restriction-p))
              (save-excursion
                (re-search-backward "[{ ,]" (line-beginning-position) t)
                (when (looking-at "[{ ,]\\([[:alnum:]._]+\\)")
                  (tg-eldoc-vertex-or-edge (tg-get-schema-element 'EdgeClass
                                                                  (match-string-no-properties 1))))))
             ;; complete keywords / functions
             (t ""))))))

(provide 'greql-mode)

;;; greql-mode.el ends here
