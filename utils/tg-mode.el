;;; tg-mode.el --- Major mode for editing TG files with emacs

;; Copyright (C) 2007 by Tassilo Horn

;; Author: Tassilo Horn <heimdall@uni-koblenz.de>

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

;; Major mode for editing TG files with Emacs


;;; History:
;; <2007-08-04 Sat>: First version

;;; Code:

(define-generic-mode tg-mode
  ;; Comments
  '(("//" . nil) ("/*" . "*/"))
  ;; Keywords
  '("AggregationClass" "Boolean" "CompositionClass" "Double" "EdgeClass"
    "EnumDomain" "Graph" "GraphClass" "Integer" "List" "Object" "RecordDomain"
    "Schema" "Set" "String" "VertexClass" "abstract" "aggregate" "from" "role"
    "to")
  ;; Additional expressions to highlight
  nil
  ;; Enable greql-mode for files matching this patterns
  '("\\.tg$")
  ;; List of functions to be run when mode is activated
  nil)

(provide 'tg-mode)

;;; tg-mode.el ends here
