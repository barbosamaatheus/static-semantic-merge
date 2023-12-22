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

### Requisitos do Projeto
É necessário que um cli do DiffJ esteja na pasta de dependências e que o diff (ferramenta de diff textual) esteja instalado.

> Se estiver utilizando o Windows, será necessário instalar o [DiffUtils](http://gnuwin32.sourceforge.net/packages/diffutils.htm) manualmente. Após a instalação, adicione o diretório de instalação ao PATH nas suas variáveis de ambiente.

Também é necessário instalar o Python versão 3.7.x ou mais recente. Isso é necessário para executar scripts que obtém arquivos de compilação e que converte os dados coletados para um formato utilizado pelas análises estáticas do SOOT invocadas por esta instância.

> Se você estiver utilizando o Windows e encontrar um erro durante a execução indicando que o Python3 não foi encontrado pelo sistema, localize a pasta de instalação do Python e crie uma cópia do arquivo "python" com o nome "python3". Isso resolverá o problema de localização do Python3 no sistema.

### Executando do Projeto

A CLI possui a seguinte página de ajuda:
```
usage: java Main
 -b <base>           the base commit
 -gp <gradlePath>    path to gradle bin
 -h <head>           the head commit
 -mp <mavenPath>     path to marge folder
 -mvp <mavenPath>    path to maven bin
 -p <parents>        the parents commits
 -ssm <mergerPath>   path to ssm folder
```

O resultado será escrito em `data/soot-results.csv`
