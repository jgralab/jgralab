;;; greql-mode.el --- Major mode for editing GReQL2 files with emacs

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

;; Major mode for editing GReQL2 files with Emacs


;;; History:
;; <2007-07-30 Mon>: First version

;;; Code:

(define-generic-mode greql-mode
  ;; Comments
  '(("//" . nil) ("/*" . "*/"))
  ;; Keywords
  '("E" "V" "as" "bag" "eSubgraph" "end" "exists!" "exists" "forall" "from" "in"
    "let" "list" "path" "pathSystem" "rec" "report" "reportBag" "reportSet"
    "set" "store" "tup" "using" "vSubgraph" "where" "with")
  ;; Additional expressions to highlight 
  '(
    ;; Funlib Functions
    ("\\<and\\>" . font-lock-builtin-face)
    ("\\<avg\\>" . font-lock-builtin-face)
    ("\\<children\\>" . font-lock-builtin-face)
    ("\\<contains\\>" . font-lock-builtin-face)
    ("\\<count\\>" . font-lock-builtin-face)
    ("\\<degree\\>" . font-lock-builtin-face)
    ("\\<depth\\>" . font-lock-builtin-face)
    ("\\<difference\\>" . font-lock-builtin-face)
    ("\\<distance\\>" . font-lock-builtin-face)
    ("\\<dividedBy\\>" . font-lock-builtin-face)
    ("\\<edgesConnected\\>" . font-lock-builtin-face)
    ("\\<edgesFrom\\>" . font-lock-builtin-face)
    ("\\<edgesTo\\>" . font-lock-builtin-face)
    ("\\<edgeTrace\\>" . font-lock-builtin-face)
    ("\\<edgeTypeSet\\>" . font-lock-builtin-face)
    ("\\<endVertex\\>" . font-lock-builtin-face)
    ("\\<equals\\>" . font-lock-builtin-face)
    ("\\<extractPath\\>" . font-lock-builtin-face)
    ("\\<getEdge\\>" . font-lock-builtin-face)
    ("\\<getValue\\>" . font-lock-builtin-face)
    ("\\<getVertex\\>" . font-lock-builtin-face)
    ("\\<grEqual\\>" . font-lock-builtin-face)
    ("\\<grThan\\>" . font-lock-builtin-face)
    ("\\<hasAttribute\\>" . font-lock-builtin-face)
    ("\\<hasType\\>" . font-lock-builtin-face)
    ("\\<id\\>" . font-lock-builtin-face)
    ("\\<inDegree\\>" . font-lock-builtin-face)
    ("\\<innerNodes\\>" . font-lock-builtin-face)
    ("\\<intersection\\>" . font-lock-builtin-face)
    ("\\<isAcyclic\\>" . font-lock-builtin-face)
    ("\\<isA\\>" . font-lock-builtin-face)
    ("\\<isCycle\\>" . font-lock-builtin-face)
    ("\\<isIn\\>" . font-lock-builtin-face)
    ("\\<isIsolated\\>" . font-lock-builtin-face)
    ("\\<isLoop\\>" . font-lock-builtin-face)
    ("\\<isNeighbour\\>" . font-lock-builtin-face)
    ("\\<isParallel\\>" . font-lock-builtin-face)
    ("\\<isPrime\\>" . font-lock-builtin-face)
    ("\\<isReachable\\>" . font-lock-builtin-face)
    ("\\<isSibling\\>" . font-lock-builtin-face)
    ("\\<isSubPathOfPath\\>" . font-lock-builtin-face)
    ("\\<isSubSet\\>" . font-lock-builtin-face)
    ("\\<isSuperSet\\>" . font-lock-builtin-face)
    ("\\<isTrail\\>" . font-lock-builtin-face)
    ("\\<isTree\\>" . font-lock-builtin-face)
    ("\\<leaves\\>" . font-lock-builtin-face)
    ("\\<leEqual\\>" . font-lock-builtin-face)
    ("\\<leThan\\>" . font-lock-builtin-face)
    ("\\<matches\\>" . font-lock-builtin-face)
    ("\\<maxPathLength\\>" . font-lock-builtin-face)
    ("\\<minPathLength\\>" . font-lock-builtin-face)
    ("\\<minus\\>" . font-lock-builtin-face)
    ("\\<modulo\\>" . font-lock-builtin-face)
    ("\\<nequals\\>" . font-lock-builtin-face)
    ("\\<nodeTrace\\>" . font-lock-builtin-face)
    ("\\<not\\>" . font-lock-builtin-face)
    ("\\<nthElement\\>" . font-lock-builtin-face)
    ("\\<or\\>" . font-lock-builtin-face)
    ("\\<outDegree\\>" . font-lock-builtin-face)
    ("\\<package-info\\>" . font-lock-builtin-face)
    ("\\<parent\\>" . font-lock-builtin-face)
    ("\\<pathConcat\\>" . font-lock-builtin-face)
    ("\\<pathLength\\>" . font-lock-builtin-face)
    ("\\<pathSystem\\>" . font-lock-builtin-face)
    ("\\<plus\\>" . font-lock-builtin-face)
    ("\\<pos\\>" . font-lock-builtin-face)
    ("\\<reachableVertices\\>" . font-lock-builtin-face)
    ("\\<reMatch\\>" . font-lock-builtin-face)
    ("\\<schemaFunctions\\>" . font-lock-builtin-face)
    ("\\<siblings\\>" . font-lock-builtin-face)
    ("\\<squareRoot\\>" . font-lock-builtin-face)
    ("\\<startVertex\\>" . font-lock-builtin-face)
    ("\\<subtypes\\>" . font-lock-builtin-face)
    ("\\<sum\\>" . font-lock-builtin-face)
    ("\\<supertypes\\>" . font-lock-builtin-face)
    ("\\<symDifference\\>" . font-lock-builtin-face)
    ("\\<times\\>" . font-lock-builtin-face)
    ("\\<toString\\>" . font-lock-builtin-face)
    ("\\<type\\>" . font-lock-builtin-face)
    ("\\<typeName\\>" . font-lock-builtin-face)
    ("\\<typeSet\\>" . font-lock-builtin-face)
    ("\\<types\\>" . font-lock-builtin-face)
    ("\\<uminus\\>" . font-lock-builtin-face)
    ("\\<union\\>" . font-lock-builtin-face)
    ("\\<vertexTypeSet\\>" . font-lock-builtin-face)
    ("\\<weight\\>" . font-lock-builtin-face)
    ("\\<xor\\>" . font-lock-builtin-face))
  ;; Enable greql-mode for files matching this patterns
  '("\\.greqlquery$" "\\.grq$" "\\.greql$")
  ;; List of functions to be run when mode is activated
  nil)

(provide 'greql-mode)

;;; greql-mode.el ends here
