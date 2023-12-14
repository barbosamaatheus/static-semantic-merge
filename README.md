# Static Semantic Merge

## Uma ferramenta para integração de análise estática ao processo de merge

A **Static Semantic Merge (SSM)**, utiliza as implementações de análise do projeto **conflict-static-analysis** para identificar interferências em cenários de integração.
> O **conflict-static-analysis** é um projeto que implementa algoritmos para analisar projetos Java, identificando possíveis conflitos durante o processo de integração.

### Objetivo

O objetivo principal do **SSM** é ser integrado ao processo de desenvolvimento de software para detectar conflitos semânticos em cenários de merge. Isso é alcançado através do uso do **conflict-static-analysis** e do **Mining Framework**.

### Integração ao Processo de Merge

O **SSM** pode ser acoplado ao processo de merge utilizando o hook de post-merge. A ideia é que o **SSM** seja executado logo após um merge sem conflito textual ser concluído.

O **SSM** consiste em quatro ações principais:

1. Gerar a build do projeto que está sendo mergeado.
2. Coletar as linhas que foram modificadas dentro do mesmo método.
3. Executar as análises.
4. Checa se as análises apontaram conflito e, se sim, reverter o commit.

### Hooks

Hooks são scripts personalizados que são disparados quando certas ações ocorrem.

O script de post-merge fica localizado em `.git/hooks` dentro do projeto e é executado pelo git logo após um merge sem conflitos textuais. O script coleta as informações necessárias e passa para o JAR do **SSM** em forma de parâmetros.

Exemplo de script de post-merge:

```bash
#!/bin/sh
head=$(git rev-parse HEAD)
parents=$(git log --pretty=%P -n 1 $head)
base=$(git merge-base $parents)
mergerPath="PATH_TO_SSM_FOLDER"
gradlePath="PATH_TO_GRADLE_BIN"
mavenPath="PATH_TO_MAVEN_BIN"
java -jar ${mergerPath}/static-semantic-merge-1.0-SNAPSHOT.jar $head $parents $base $mergerPath $gradlePath $mavenPath
```
### Geração de Build com Gradle

Para gerar a build do projeto com Gradle, utilize o seguinte comando na raiz do projeto:

```bash
./gradlew build
```
### Estrutura do Projeto

Na raiz do projeto, você encontrará o script de exemplo post-merge, além das seguintes pastas:

StudyRun: Contém scripts para a execução de cenários específicos.
dependencies: Inclui os scripts e os JARs das dependências (Mining Framework, Conflict Static Analysis, DiffJ e GroovyCSV).