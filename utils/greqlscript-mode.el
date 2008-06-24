;;; greqlscript-mode.el --- Major mode for editing greqlscript files with emacs

;; Copyright (C) 2008 by Tassilo Horn

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

;; Major mode for editing greqlscript files with emacs


;;; History:
;; <2008-06-23 Mon 13:01>: First version

;;; Code:

(define-generic-mode greqlscript-mode
  ;; Comments
  '(("//" . nil) ("/*" . "*/"))
  ;; Keywords
  '("assert" "break" "case" "continue" "default" "do" "else"
    "foreach" "hastype" "if" "import" "importjava" "return"
    "typeswitch" "var" "while")
  ;; Additional expressions to highlight 
  '()
  ;; Enable greql-mode for files matching this patterns
  '("\\.greqlscript$")
  ;; List of functions to be run when mode is activated
  nil)

(provide 'greqlscript-mode)

;;; greqlscript-mode.el ends here
