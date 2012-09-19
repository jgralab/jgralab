Transition JGraLab 6 (Falcarius) -> JGraLab 7 (Giraffatitan)

API Changes:
============

- TRANSACTION and DATABASE implementation discontinued, only STANDARD and GENERIC implementations remain
- Exceptions moved to "jgralab.exceptions" package, some exceptions renamed
- Cleanup in utilities
  - GreqlGui: package renamed from ...utilities.greqlinterface to ...utilities.greqlgui
  - Rsa2TG: package renamed from ...utilities.rsa to ...utilities.rsa2tg
  - GreqlServer: moved to separate package ...utilities.greqlserver
  - Various rarely used utilities moved to "jgralab-museum" (internal project @UKo)


Actions to converting projects depending on JGraLab from 6.x.x to 7.x.x:
========================================================================

- Update projects jgralab and common
- In your build.xml files, remove "implementationType" parameter from tgschema2java tasks
- In case you use TgSchema2Java without the predefined tasks, remove "implementationType" parameter if existent
- Re-generate schema code, if appropriate
- Update import declarations for old JGraLab top-level exceptions to package ...jgralab.exceptions
- Re-compile your project


