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
;; <2009-12-10 Thu 09:55>

;;; TODO:
;; - Implement handling of imports in completion (DONE) and highlighting (still
;;   not done)

;;; Code:

;; TG mode contains the schema parsing stuff.
(require 'tg-mode)

(defparameter greql-keywords
  '("E" "V" "as" "bag" "eSubgraph" "end" "exists!" "exists" "forall"
    "from" "in" "let" "list" "path" "pathSystem" "rec" "report"
    "reportBag" "reportSet" "reportMap" "set" "store" "tup" "using"
    "vSubgraph" "where" "with" "thisEdge" "thisVertex" "map" "import"
    "true" "false" "null")
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

(defparameter greql-functions (greql-functions)
  "GReQL functions that should be completed and highlighted.")
(put 'greql-functions 'risky-local-variable-p t)

(dolist (ext '("\\.greqlquery$" "\\.grq$" "\\.greql$"))
  (add-to-list 'auto-mode-alist (cons ext 'greql-mode)))

(defparameter greql-fontlock-keywords-1
  `(
    ;; Highlight function names
    ,(cons (concat "\\<" (regexp-opt greql-functions t) "\\>")
           font-lock-function-name-face)
    ;; Highlight strings
    ,(list "\".*?\"" 0 font-lock-string-face t)
    ;; Highlight one-line comments
    ,(list "//.*$" 0 font-lock-comment-face t)))

(defparameter greql-fontlock-keywords-2
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
                                  (dolist (i tg-schema-alist)
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

(defun greql-initialize-schema ()
  (when (and greql-graph (not tg-schema-alist))
    (greql-set-graph greql-graph)
    (greql-set-fontlock-keywords-3)))

(defun greql-set-graph (graph)
  "Set `greql-graph' to GRAPH and parse it with `tg-parse-schema'."
  (interactive "fGraph file: ")
  (setq greql-graph graph)
  (let ((g greql-graph)
        schema-alist)
    (with-temp-buffer
      (insert-file-contents g)
      (setq schema-alist (tg-parse-schema)))
    (setq tg-schema-alist schema-alist))
  ;; add keywords and functions, too
  (dolist (key greql-keywords)
    (setq tg-schema-alist (cons (list 'keyword key)
                                   tg-schema-alist)))
  (dolist (fun greql-functions)
    (setq tg-schema-alist (cons (list 'funlib fun)
                                   tg-schema-alist)))

  (greql-set-fontlock-keywords-3))


(defun greql-import-completion-list (&optional types)
  "Additional completions due to imports."
  (let ((comp-lst (greql-completion-list types))
        lst)
    (save-excursion
      (goto-char 0)
      (while (re-search-forward "import\s+\\([^;]+\\);" nil t)
        (let ((import (match-string-no-properties 1)))
          (if (string-match "\\*$" import)
              ;; A package import
              (setq
               lst
               (nconc lst
                      (delq nil
                            (mapcar
                             (lambda (str)
                               (let ((regex (regexp-quote
                                             (substring
                                              import 0 (- (length import) 1)))))
                                 (if (string-match regex str)
                                     (replace-regexp-in-string regex "" str)
                                   nil)))
                             comp-lst))))
            ;; An element import
            (when (member import comp-lst)
              (setq lst (cons
                         (replace-regexp-in-string "\\([[:word:]._]+\\.\\)\\([^.]+\\)" "\\2" import)
                         lst)))))))
    lst))

(defun greql-completion-list (&optional types)
  (when tg-schema-alist
    (let (completions)
      (dolist (line tg-schema-alist)
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
        ;; Do nothing here.
        nil)
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
   ;; Complete any classes
   ((greql-import-p)
    (greql-complete-anyclass))
   ;; complete attributes
   ((greql-variable-p)
    (let* ((vartypes (greql-variable-types))
           (attrs (tg-attributes vartypes)))
      (greql-complete-1 attrs "[.]")))
   ;; complete keywords / functions
   (t (greql-complete-keyword-or-function))))

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
  (let ((types '(EnumDomain RecordDomain ListDomain SetDomain BagDomain)))
    (greql-complete-1 (nconc (greql-import-completion-list types)
                             (greql-completion-list types)))))

(defun greql-complete-keyword-or-function ()
  (interactive)
  (greql-complete-1 (greql-completion-list '(keyword funlib))))

(defvar greql-process nil
  "Network process to the GreqlEvalServer.")
(make-variable-buffer-local 'greql-process)

(defun greql-execute ()
  "Execute the query in the current buffer on `greql-graph'.
If a region is active, use only that as query."
  (interactive)
  (let ((buffer (get-buffer-create greql-buffer))
        (queryfile (if (region-active-p)
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
  (looking-back "import\s+[[:word:]._]*"))

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
          (list mtype (split-string types "[,]")))))))

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
