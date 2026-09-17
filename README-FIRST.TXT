Archetyp pro tvorbu "jednoduchého" JAR souboru bez Springu používající javu 17.

Obsahuje:
- ukázku testů pomocí JUnit5 (100% test code coverage)
- kontrolu pomocí analyzátorů kvality kódu PMD a SpotBugs
- kontrolu code coverage (100%), pro nižší nastavení modifikujte <code-coverage>1.0</code-coverage>
- logování pomocí SLF4J, LOGBACK

Pokyny:
- pokud přidáte novou závislost do projektu, proveďte hned build aplikace, aby license-plugin mohl provést stažení její licence 
do src/main/resources/licenses/ a ověření, že tato licence patří mezi povolené
- závislosti, které budou součástí výsledné aplikace (netýká se tedy testů) musí používat pouze licence schválené architektem!
- ověřte, že výsledný build skutečně obsahuje všechny licence všech závislostí
- build jako takový vyžaduje, aby každá metoda v každé třídě (mimo hlavního core balíčku) byla otestována, toto lze dočasně 
upravit nastavením code-coverage na nižší hodnotu a dále nastavením vyjímek v exclude u jacoco pluginu
- před distribucí k zákazníkům musí být code coverage 100%
