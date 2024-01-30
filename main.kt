import java.io.File
import kotlin.math.max

// constantes que controlam os diferentes estados/menus do jogo
const val MENU_PRINCIPAL = 100
const val MENU_DEFINIR_TABULEIRO = 101
const val MENU_DEFINIR_NAVIOS = 102
const val MENU_JOGAR = 103
const val MENU_LER_FICHEIRO = 104
const val MENU_GRAVAR_FICHEIRO = 105
const val SAIR = 106

const val PERGUNTAR_POR_COORDENADAS = "Coordenadas? (ex: 6,G)"

// variáveis globais que armazenam as dimensões do tabuleiro
var numLinhas = -1
var numColunas = -1

// tabuleiros de jogo para o humano e para o computador
var tabuleiroHumano: Array<Array<Char?>> = emptyArray()
var tabuleiroComputador: Array<Array<Char?>> = emptyArray()

// tabuleiros para armazenar os palpites (tentativas de tiro) do humano e do computador
var tabuleiroPalpitesDoHumano: Array<Array<Char?>> = emptyArray()
var tabuleiroPalpitesDoComputador: Array<Array<Char?>> = emptyArray()

//FUNÇÕES DE VERIFICAÇÃO E UTILIDADE

// função que verifica se as dimensões do tabuleiro estão de acordo com as regras do jogo
fun tamanhoTabuleiroValido(numLinhas: Int, numColunas: Int): Boolean {

    return (numLinhas == 4 && numColunas == 4) ||
            (numLinhas == 5 && numColunas == 5) ||
            (numLinhas == 7 && numColunas == 7) ||
            (numLinhas == 8 && numColunas == 8) ||
            (numLinhas == 10 && numColunas == 10)
}

// função que verifica se uma coordenada específica (linha e coluna) está contida dentro dos limites do tabuleiro
fun coordenadaContida(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {
    // obtém o número de linhas e colunas do tabuleiro
    val numLinhas = tabuleiro.size
    val numColunas = tabuleiro[0].size

    // verifica se a linha e a coluna estão dentro dos limites do tabuleiro
    return linha in 1..numLinhas && coluna in 1..numColunas
}

// função que processa as coordenadas inseridas pelo utilizador, verificando se são validas ou não
fun processaCoordenadas(coordenadas: String?, numLinhas: Int, numColunas: Int): Pair<Int, Int>? {
    // verifica se a string de coordenadas é nula ou tem um comprimento inesperado
    if (coordenadas == null || coordenadas.length !in 3..4) {
        return null
    }

    val linha: Int
    val colunaChar: Char

    // verifica se a vírgula está na posição correta para separar a linha e a coluna
    if ((coordenadas.length == 3 && coordenadas[1] != ',') || (coordenadas.length == 4 && coordenadas[2] != ',')) {
        return null
    }

    // extrai os valores de linha e coluna da string, tratando coordenadas de um ou dois dígitos
    if (coordenadas.length == 4) {
        linha =
            coordenadas.substring(0, 2).toIntOrNull() ?: return null // extrai a linha para coordenadas de dois dígitos
        colunaChar = coordenadas[3].uppercaseChar()
    } else {
        linha = coordenadas[0].toString().toIntOrNull() ?: return null // extrai a linha para coordenadas de um dígito
        colunaChar = coordenadas[2].uppercaseChar()
    }

    // verifica se a linha está dentro do intervalo válido do tabuleiro
    if (linha !in 1..numLinhas) {
        return null
    }

    // calcula o índice da coluna convertendo o caractere para sua posição alfabética
    val coluna = colunaChar - 'A' + 1
    if (coluna !in 1..numColunas) {
        return null
    }

    // retorna o par de coordenadas se todas as verificações passarem
    return Pair(linha, coluna)
}

// função usada para verificar se todas as coordenadas especificadas num array estão livres
fun estaLivre(tabuleiro: Array<Array<Char?>>, coordenadaLivre: Array<Pair<Int, Int>>): Boolean {
    // itera sobre cada par de coordenadas fornecido
    for (coordenada in coordenadaLivre) {
        // ajusta as coordenadas para índices baseados em 0, pois os arrays em Kotlin são indexados a partir de 0
        val linha = coordenada.first - 1
        val coluna = coordenada.second - 1

        // obtém a linha do tabuleiro, retorna false se a linha estiver fora dos limites do tabuleiro
        val linhaTabuleiro = tabuleiro.getOrNull(linha) ?: return false

        // verifica se a célula especificada no tabuleiro está ocupada ou fora dos limites
        if (linhaTabuleiro.getOrNull(coluna) != null) {
            return false // retorna false se a célula estiver ocupada
        }
    }
    // retorna true se todas as coordenadas estiverem livres
    return true
}

// função responsável por determinar a quantidade e os tipos de navios que devem ser usados no jogo, com base no tamanho do tabuleiro
fun calculaNumNavios(numLinhas: Int, numColunas: Int): Array<Int> {
    // verifica se o tabuleiro é quadrado, se não for retorna um array vazio
    if (numLinhas != numColunas) {
        return emptyArray()
    }

    // retorna a quantidade de navios com base no tamanho do tabuleiro
    return when (numLinhas) {
        4 -> arrayOf(2, 0, 0, 0) // 2 submarinos
        5 -> arrayOf(1, 1, 1, 0) // 1 submarino, 1 contra-torpedeiro e 1 navio-tanque
        7 -> arrayOf(2, 1, 1, 1) // 2 submarinos, 1 contra-torpedeiro, 1 navio-tanque e 1 porta-aviões
        8 -> arrayOf(2, 2, 1, 1) // 2 submarinos e contra-torpedeiros, e 1 navio-tanque e porta-aviões
        10 -> arrayOf(3, 2, 1, 1) // 3 submarinos, 2 contra-torpedeiros, 1 navio-tanque, e 1 porta-aviões
        else -> emptyArray() // retorna vazio para tamanhos de tabuleiro não suportados
    }
}

// função que combina dois arrays de coordenadas num único array
fun juntarCoordenadas(
    arrayCoordenadas: Array<Pair<Int, Int>>,
    parCoordenadas: Array<Pair<Int, Int>>
): Array<Pair<Int, Int>> {
    return arrayCoordenadas + parCoordenadas
}

// função utilizada para remover pares de coordenadas que representam posições inválidas ou vazias de um array de coordenadas
fun limparCoordenadasVazias(coordenadas: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {
    // conta quantas coordenadas não são vazias
    var count = 0
    for (par: Pair<Int, Int> in coordenadas) {
        if (par.first != 0 || par.second != 0) {
            count++
        }
    }

    // cria um novo array para armazenar as coordenadas não vazias
    val paresLimpos = Array(count) { Pair(0, 0) }
    var index = 0

    // copia as coordenadas não vazias para o novo array
    for (par: Pair<Int, Int> in coordenadas) {
        if (par.first != 0 || par.second != 0) {
            paresLimpos[index] = par
            index++
        }
    }

    // retorna o array com as coordenadas não vazias
    return paresLimpos
}

// função que conta quantos navios de uma certa dimensão existem em um tabuleiro
fun contarNaviosDeDimensao(tabuleiro: Array<Array<Char?>>, dimensao: Int): Int {
    var count = 0 // contador para o número de navios encontrados

    // itera sobre cada célula do tabuleiro
    for (linha in tabuleiro.indices) {
        for (coluna in tabuleiro[linha].indices) {
            val celula = tabuleiro[linha][coluna]

            // verifica se a célula atual contém um dígito e corresponde ao tamanho do navio desejado
            if (celula != null && celula.isDigit() && celula.toString().toInt() == dimensao) {
                // verifica se o navio cabe no tabuleiro na orientação horizontal
                var navioHorizontalValido = coluna + dimensao <= tabuleiro[linha].size
                // verifica se o navio cabe no tabuleiro na orientação vertical
                var navioVerticalValido = linha + dimensao <= tabuleiro.size

                // verifica se todas as células subsequentes na horizontal contêm o mesmo dígito
                for (i in 1 until dimensao) {
                    if (navioHorizontalValido) {
                        navioHorizontalValido = tabuleiro[linha][coluna + i] == celula
                    }
                }

                // verifica se todas as células subsequentes na vertical contêm o mesmo dígito
                for (i in 1 until dimensao) {
                    if (navioVerticalValido) {
                        navioVerticalValido = tabuleiro[linha + i][coluna] == celula
                    }
                }

                // incrementa o contador se o navio encontrado corresponder à dimensão desejada
                if (navioHorizontalValido || navioVerticalValido) {
                    count++
                }
            }
        }
    }

    return count // retorna o número total de navios encontrados
}

//FUNÇÕES DE CRIAÇÃO E CONFIGURAÇÃO DO TABULEIRO

// função que cria um tabuleiro de jogo vazio com o número especificado de linhas e colunas esta na obtemmapa
fun criaTabuleiroVazio(numLinhas: Int, numColunas: Int): Array<Array<Char?>> {
    // cria um array bidimensional com o tamanho especificado, inicializando todas as células como null
    return Array(numColunas) { arrayOfNulls<Char?>(numLinhas) }
}

// função responsável por criar uma representação visual do tabuleiro/ implementado com a logica da criaterreno
fun obtemMapa(tabuleiro: Array<Array<Char?>>, tabuleiroCerto: Boolean): Array<String> {
    // determina o número de linhas e colunas do tabuleiro
    val numLinhas = tabuleiro.size
    val numColunas = if (numLinhas > 0) tabuleiro[0].size else 0

    // constrói a linha do cabeçalho com as letras das colunas
    val cabecalho = "| " + ('A' until 'A' + numColunas).joinToString(" | ") + " |"
    // inicializa o array do mapa, adicionando espaço para a linha do cabeçalho
    val mapa = Array(numLinhas + 1) { "" }
    mapa[0] = cabecalho
        // itera sobre cada linha do tabuleiro
        for (linha in 0 until numLinhas) {
            var linhaMapa = "| "
            // itera sobre cada coluna na linha
            for (coluna in 0 until numColunas) {
                // obtém o conteúdo da célula do tabuleiro
                val celula = tabuleiro[linha][coluna]
                // determina a representação visual da célula
                val representacaoCelula = when {
                    // para o tabuleiro principal, mostra o conteúdo da célula ou água se estiver vazia
                    tabuleiroCerto -> celula?.toString() ?: "~"
                    // para o tabuleiro de palpites, mostra "?" se não houver palpite, ou a representação do navio
                    else -> {
                        if (celula == null) {
                            "?"
                        } else if (navioCompleto(tabuleiro, linha + 1, coluna + 1)) {
                            celula.toString()
                        } else {
                            // representa navios não completos com números minusculos da tabela ASCII
                            when (celula) {
                                '1' -> "\u2081"
                                '2' -> "\u2082"
                                '3' -> "\u2083"
                                '4' -> "\u2084"
                                else -> celula.toString()
                            }
                        }
                    }
                }
                // constrói a linha do mapa, adicionando a representação de cada célula
                linhaMapa += "$representacaoCelula | "
            }
            // adiciona o número da linha ao final de cada linha do mapa
            linhaMapa += "${linha + 1}"
            mapa[linha + 1] = linhaMapa
        }

        // retorna o mapa completo
        return mapa
}

//FUNÇÕES DE MANIPULAÇÃO DE NAVIOS

// função que gera um array de coordenadas para um navio
fun gerarCoordenadasNavio(
    tabuleiro: Array<Array<Char?>>,
    linha: Int,
    coluna: Int,
    orientacao: String,
    dimensao: Int
): Array<Pair<Int, Int>> {
    // inicializa um array de pares de coordenadas com o tamanho do navio
    val coordenadas = Array(dimensao) { Pair(0, 0) }

    // determina o número de linhas e colunas do tabuleiro
    val numLinhas = tabuleiro.size
    val numColunas = tabuleiro[0].size

    // itera sobre cada parte do navio para determinar as suas coordenadas
    for (i in 0 until dimensao) {
        val novaLinha: Int
        val novaColuna: Int

        // calcula as coordenadas de cada parte do navio com base na orientação
        when (orientacao) {
            "N" -> {
                novaLinha = linha - i - 1
                novaColuna = coluna - 1
            }

            "S" -> {
                novaLinha = linha + i - 1
                novaColuna = coluna - 1
            }

            "E" -> {
                novaLinha = linha - 1
                novaColuna = coluna + i - 1
            }

            "O" -> {
                novaLinha = linha - 1
                novaColuna = coluna - i - 1
            }

            else -> return emptyArray() // retorna um array vazio para orientações inválidas
        }

        // verifica se as coordenadas calculadas estão dentro dos limites do tabuleiro.
        if (novaLinha !in 0 until numLinhas || novaColuna !in 0 until numColunas) {
            return emptyArray() // retorna um array vazio se as coordenadas estiverem fora do tabuleiro
        }

        // adiciona as coordenadas calculadas ao array de coordenadas
        coordenadas[i] = Pair(novaLinha + 1, novaColuna + 1)
    }

    // retorna o array completo de coordenadas do navio
    return coordenadas
}

// função usada para gerar um conjunto de coordenadas que representam a fronteira ao redor de um navio
fun gerarCoordenadasFronteira(
    tabuleiro: Array<Array<Char?>>,
    linha: Int,
    coluna: Int,
    orientacao: String,
    dimensao: Int
): Array<Pair<Int, Int>> {
    // gera as coordenadas do navio com base nos parâmetros fornecidos
    val coordenadasNavio = gerarCoordenadasNavio(tabuleiro, linha, coluna, orientacao, dimensao)
    var coordenadasFronteira = emptyArray<Pair<Int, Int>>()

    // itera sobre cada coordenada do navio para encontrar as células adjacentes
    for (par in coordenadasNavio) {
        val (linhaNavio, colunaNavio) = par
        // verifica todas as células ao redor da célula atual (incluindo diagonais)
        for (deltaLinha in -1..1) {
            for (deltaColuna in -1..1) {
                val linhaAdj = linhaNavio + deltaLinha
                val colunaAdj = colunaNavio + deltaColuna
                // verifica se a célula adjacente está dentro dos limites do tabuleiro e não é parte do navio
                if (coordenadaContida(tabuleiro, linhaAdj, colunaAdj) && Pair(
                        linhaAdj,
                        colunaAdj
                    ) !in coordenadasNavio
                ) {
                    // adiciona a célula adjacente às coordenadas da fronteira
                    coordenadasFronteira = juntarCoordenadas(coordenadasFronteira, arrayOf(Pair(linhaAdj, colunaAdj)))
                }
            }
        }
    }

    // limpa e retorna as coordenadas da fronteira, removendo duplicatas e coordenadas inválidas
    return limparCoordenadasVazias(coordenadasFronteira)
}

// função usada para colocar um navio no tabuleiro do jogo, considerando a linha, a coluna, a orientação e o tamanho do navio
fun insereNavio(
    tabuleiro: Array<Array<Char?>>,
    linha: Int,
    coluna: Int,
    orientacao: String,
    dimensao: Int
): Boolean {
    // gera as coordenadas do navio com base na orientação e dimensão
    val coordenadasNavio = gerarCoordenadasNavio(tabuleiro, linha, coluna, orientacao, dimensao)

    // verifica se as coordenadas do navio estão dentro dos limites do tabuleiro
    if (coordenadasNavio.isEmpty()) return false

    // gera as coordenadas da fronteira ao redor do navio
    val coordenadasFronteira = gerarCoordenadasFronteira(tabuleiro, linha, coluna, orientacao, dimensao)

    // combina as coordenadas do navio com as coordenadas da fronteira
    val todasCoordenadas = juntarCoordenadas(coordenadasNavio, coordenadasFronteira)

    // verifica se todas as coordenadas combinadas estão livres
    if (estaLivre(tabuleiro, todasCoordenadas)) {
        // insere o navio no tabuleiro
        for (coordenada in coordenadasNavio) {
            val (linhaNavio, colunaNavio) = coordenada
            tabuleiro[linhaNavio - 1][colunaNavio - 1] = dimensao.toString().first()
        }
        return true
    }

    return false
}

// função que insere um navio no tabuleiro de Batalha Naval numa orientação horizontal
fun insereNavioSimples(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int, dimensao: Int): Boolean {
    // verifica se o navio cabe no tabuleiro a partir da posição inicial
    if (coluna - 1 + dimensao > tabuleiro[0].size) {
        return false // o navio ultrapassaria os limites do tabuleiro na horizontal
    }

    // verifica se a linha especificada está dentro dos limites do tabuleiro
    if (linha !in 1..tabuleiro.size) {
        return false // a linha especificada está fora dos limites do tabuleiro
    }

    // verifica se todas as células onde o navio será inserido estão livres
    for (i in 0 until dimensao) {
        val celula = tabuleiro.getOrNull(linha - 1)?.getOrNull(coluna - 1 + i)
        if (celula != null) {
            return false // uma ou mais posições estão ocupadas ou fora dos limites
        }
    }

    // insere o navio no tabuleiro na orientação horizontal
    for (i in 0 until dimensao) {
        tabuleiro[linha - 1][coluna - 1 + i] = dimensao.toString().first()
    }

    return true // retorna true indicando que o navio foi inserido com sucesso
}

// função que preenche o tabuleiro do computador de maneira aleatoria
fun preencheTabuleiroComputador(tabuleiro: Array<Array<Char?>>, numNavio: Array<Int>) {
    // define as possíveis direções para os navios: Norte, Sul, Leste, Oeste
    val direcoesPossiveis = arrayOf("N", "S", "E", "O")

    // itera sobre os diferentes tamanhos de navios.
    for (tamanhoNavio in numNavio.indices) {
        // determina quantos navios deste tamanho precisam ser colocados
        var naviosParaColocar = numNavio[tamanhoNavio]

        // continua a colocar navios até que o número desejado seja alcançado
        while (naviosParaColocar > 0) {
            // gera uma posição aleatória para o navio.
            val linhaAleatoria = (1..tabuleiro.size).random()
            val colunaAleatoria = (1..tabuleiro[0].size).random()
            // escolhe uma direção aleatória para o navio.
            val direcaoAleatoria = direcoesPossiveis.random()

            // gera as coordenadas do navio com base na posição e direção
            val coordenadasNavio =
                gerarCoordenadasNavio(tabuleiro, linhaAleatoria, colunaAleatoria, direcaoAleatoria, tamanhoNavio + 1)
            // gera as coordenadas adjacentes ao navio para garantir espaço ao redor dele
            val coordenadasAdjacentes =
                gerarCoordenadasFronteira(
                    tabuleiro,
                    linhaAleatoria,
                    colunaAleatoria,
                    direcaoAleatoria,
                    tamanhoNavio + 1
                )
            // combina as coordenadas do navio com suas coordenadas adjacentes
            val todasCoordenadas = coordenadasNavio + coordenadasAdjacentes

            // verifica se as coordenadas do navio e as adjacentes estão livres
            if (coordenadasNavio.isNotEmpty() && estaLivre(tabuleiro, todasCoordenadas)) {
                // Coloca o navio no tabuleiro
                for (index in coordenadasNavio.indices) {
                    val (linhaNavio, colunaNavio) = coordenadasNavio[index]
                    tabuleiro[linhaNavio - 1][colunaNavio - 1] = (tamanhoNavio + 1).toString().first()
                }
                // decrementa o contador de navios a colocar
                naviosParaColocar--
            }
        }
    }
}

//FUNÇÕES DE INTERAÇÃO DO JOGO

// função que pede ao utilizador coordenadas válidas para a posição de um navio e a sua orientação
fun obterCoordenadasValidas(numLinhas: Int, numColunas: Int, tamanhoNavio: Int): Pair<Pair<Int, Int>?, String?> {
    var coordenadas: Pair<Int, Int>? = null
    while (coordenadas == null) {
        println(PERGUNTAR_POR_COORDENADAS)
        val inputCoordenadas = readlnOrNull()
        if (inputCoordenadas == "-1") return Pair(null, null) // retorna null para ambos se o utilizador quiser sair
        coordenadas = processaCoordenadas(inputCoordenadas, numLinhas, numColunas)
        if (coordenadas == null) println("!!! Coordenadas invalidas, tente novamente")
    }

    var orientacao: String? = null
    if (tamanhoNavio > 1) { // se o navio for maior que uma célula (que um submarino), pede a orientação
        println("Insira a orientacao do navio:")
        while (orientacao == null) {
            println("Orientacao? (N, S, E, O)")
            val inputOrientacao = readlnOrNull()
            if (inputOrientacao == "-1") return Pair(null, null) // retorna null para ambos se o utilizador deseja sair
            if (inputOrientacao in arrayOf("N", "S", "E", "O")) {
                orientacao = inputOrientacao
            } else {
                println("!!! Orientacao invalida, tente novamente")
            }
        }
    }

    return Pair(coordenadas, orientacao) // retorna as coordenadas e a orientação se forem válidas
}

// função que verifica se um navio inteiro foi atingido no tabuleiro
fun navioCompleto(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {
    // obtém o caractere do navio na coordenada especificada. Retorna false se for nulo ou não numérico
    val navio = tabuleiro.getOrNull(linha - 1)?.getOrNull(coluna - 1) ?: return false
    val tamanhoNavio = navio.toString().toIntOrNull() ?: return false

    // define as direções para verificar (Norte, Sul, Leste, Oeste)
    val direcoes = arrayOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))

    var contadorPartesNavio = 1 // conta a célula inicial como parte do navio

    // itera sobre cada direção
    for (direcao in direcoes) {
        var linhaAtual = linha
        var colunaAtual = coluna
        var deveContinuar = true

        // continua a mover-se na direção atual enquanto apropriado
        while (deveContinuar) {
            linhaAtual += direcao.first
            colunaAtual += direcao.second

            // verifica se a nova posição está dentro dos limites do tabuleiro e se a célula contém o mesmo navio
            if (linhaAtual !in 1..tabuleiro.size || colunaAtual !in 1..tabuleiro[0].size || tabuleiro[linhaAtual - 1][colunaAtual - 1] != navio) {
                deveContinuar = false // encerra a busca na direção atual se a condição falhar
            } else if (linhaAtual != linha || colunaAtual != coluna) {
                contadorPartesNavio++ // incrementa o contador de partes se a célula contiver parte do mesmo navio
            }
        }
    }

    // verifica se o contador de partes corresponde ao tamanho do navio
    return contadorPartesNavio == tamanhoNavio
}

// função usada para simular o lançamento de um tiro e e atualizar o tabuleiro de palpites com o resultado
fun lancarTiro(
    tabuleiroReal: Array<Array<Char?>>,
    tabuleiroPalpites: Array<Array<Char?>>,
    coordenadas: Pair<Int, Int>
): String {
    // extrai as coordenadas de linha e coluna do tiro
    val (linha, coluna) = coordenadas
    val linhaIndex = linha - 1
    val colunaIndex = coluna - 1

    // verifica se o tiro está dentro dos limites do tabuleiro
    if (linhaIndex !in tabuleiroReal.indices || colunaIndex !in tabuleiroReal[0].indices) {
        return "Fora do tabuleiro"
    }

    // obtém o conteúdo da célula alvo no tabuleiro real
    val alvo = tabuleiroReal[linhaIndex][colunaIndex]

    // atualiza o tabuleiro de palpites e retorna a mensagem correspondente ao resultado do tiro
    return when (alvo) {
        null -> {
            tabuleiroPalpites[linhaIndex][colunaIndex] = 'X' // tiro na água
            "Agua."
        }

        '1' -> {
            tabuleiroPalpites[linhaIndex][colunaIndex] = '1' // certou no submarino
            "Tiro num submarino."
        }

        '2' -> {
            tabuleiroPalpites[linhaIndex][colunaIndex] = '2' // acertou no contra-torpedeiro
            "Tiro num contra-torpedeiro."
        }

        '3' -> {
            tabuleiroPalpites[linhaIndex][colunaIndex] = '3' // acertou no navio-tanque
            "Tiro num navio-tanque."
        }

        '4' -> {
            tabuleiroPalpites[linhaIndex][colunaIndex] = '4' // acertou no porta-aviões
            "Tiro num porta-avioes."
        }

        else -> "" // retorna uma string vazia se a célula contiver um valor inesperado
    }
}

// função que gera um tiro aleatório pelo computador
fun geraTiroComputador(tabuleiroPalpitesPC: Array<Array<Char?>>): Pair<Int, Int> {
    // determina o número de linhas e colunas do tabuleiro
    val numLinhas = tabuleiroPalpitesPC.size
    val numColunas = if (numLinhas > 0) tabuleiroPalpitesPC[0].size else 0

    // contabiliza as células que ainda não foram alvo de tiro (células disponíveis)
    var celulasDisponiveis = 0
    for (linha in tabuleiroPalpitesPC) {
        for (celula in linha) {
            if (celula == null) {
                celulasDisponiveis++
            }
        }
    }

    // verifica se há células disponíveis para atirar
    if (celulasDisponiveis == 0) {
        return Pair(-1, -1) // Retorna um par inválido se não houver células disponíveis
    }

    // gera um índice aleatório correspondente a uma das células disponíveis
    val indiceAleatorio = (1..celulasDisponiveis).random()

    // percorre o tabuleiro para encontrar a célula que corresponde ao índice aleatório
    var contador = 0
    for (linha in 0 until numLinhas) {
        for (coluna in 0 until numColunas) {
            if (tabuleiroPalpitesPC[linha][coluna] == null) {
                contador++
                if (contador == indiceAleatorio) {
                    return Pair(linha + 1, coluna + 1) // retorna as coordenadas do tiro
                }
            }
        }
    }

    // este ponto só será atingido em caso de erro lógico
    return Pair(-1, -1)
}

// função determina se todos os navios no tabuleiro foram atingidos
fun venceu(tabuleiro: Array<Array<Char?>>): Boolean {
    // obtém o número de linhas e colunas do tabuleiro
    val numLinhas = tabuleiro.size
    val numColunas = if (tabuleiro.isNotEmpty()) tabuleiro[0].size else 0

    // calcula o número de navios esperados por dimensão com base no tamanho do tabuleiro
    val numNaviosPorDimensao = calculaNumNavios(numLinhas, numColunas)

    // itera sobre cada dimensão de navio.
    for (i in numNaviosPorDimensao.indices) {
        // determina o tamanho do navio para a dimensão atual
        val tamanhoNavio = i + 1

        // conta quantos navios dessa dimensão foram atingidos no tabuleiro
        val numNaviosAtingidos = contarNaviosDeDimensao(tabuleiro, tamanhoNavio)

        // verifica se o número de navios atingidos corresponde ao número esperado para essa dimensão
        if (numNaviosAtingidos != numNaviosPorDimensao[i]) { // condição vencer jogo
            return false // retorna false se qualquer dimensão de navio não estiver completamente atingida
        }
    }

    return true // retorna true se todos os navios de todas as dimensões foram atingidos
}

// FUNÇÕES DE LEITURA E GRAVAÇÃO DO JOGO

// função projetada para ler o estado de jogo de um arquivo de texto e converter as informações lidas num tabuleiro
fun lerJogo(nomeDoFicheiro: String, tipoDeTabuleiro: Int): Array<Array<Char?>> {
    // lê todas as linhas do arquivo
    val linhasDoArquivo = File(nomeDoFicheiro).readLines()

    // extrai o tamanho do tabuleiro da primeira linha do arquivo
    val dimensaoTabuleiro = linhasDoArquivo[0].split(",")[0].toInt()

    // ajusta as variáveis globais para o tamanho do tabuleiro
    numLinhas = dimensaoTabuleiro
    numColunas = dimensaoTabuleiro

    // calcula a linha de início no arquivo para o tipo de tabuleiro especificado
    val inicioLeitura = 4 + (dimensaoTabuleiro + 3) * (tipoDeTabuleiro - 1)

    // inicializa o tabuleiro com células vazias
    val matrizTabuleiro = Array(dimensaoTabuleiro) { arrayOfNulls<Char?>(dimensaoTabuleiro) }

    // lê o tabuleiro linha por linha a partir da linha de início calculada
    var contadorLinha = inicioLeitura
    while (contadorLinha < dimensaoTabuleiro + inicioLeitura) {
        // divide a linha atual em células, limitando ao tamanho do tabuleiro
        val elementosLinha = linhasDoArquivo[contadorLinha]
            .split(",", limit = dimensaoTabuleiro)
            .map { it.firstOrNull() } // Converte cada elemento para Char? (null se vazio)

        // preenche a linha correspondente no tabuleiro com os elementos da linha do arquivo
        for (indiceColuna in elementosLinha.indices) {
            matrizTabuleiro[contadorLinha - inicioLeitura][indiceColuna] = elementosLinha[indiceColuna]
        }
        contadorLinha++
    }

    // retorna o tabuleiro preenchido
    return matrizTabuleiro
}

// função usada para guardar o estado atual de um jogo
fun gravarJogo(
    nomeDoFicheiro: String,
    tabuleiroHumano: Array<Array<Char?>>,
    tabuleiroPalpitesDoHumano: Array<Array<Char?>>,
    tabuleiroComputador: Array<Array<Char?>>,
    tabuleiroPalpitesDoComputador: Array<Array<Char?>>
) {
    // cria um escritor para gravar no arquivo especificado
    val escritor = File(nomeDoFicheiro).printWriter()

    // escreve as dimensões do tabuleiro no arquivo
    escritor.println("${tabuleiroHumano.size},${tabuleiroHumano.size}")
    escritor.println()

    // grava o tabuleiro do humano
    escritor.println("Jogador")
    escritor.println("Real")
    for (linha in tabuleiroHumano.indices) {
        // junta os elementos de cada linha do tabuleiro, separando-os por vírgulas
        val linhaTabuleiro = tabuleiroHumano[linha].joinToString(",") { it?.toString() ?: "" }
        escritor.println(linhaTabuleiro)
    }

    // grava o tabuleiro de palpites do humano
    escritor.println()
    escritor.println("Jogador")
    escritor.println("Palpites")
    for (linha in tabuleiroPalpitesDoHumano.indices) {
        val linhaPalpites = tabuleiroPalpitesDoHumano[linha].joinToString(",") { it?.toString() ?: "" }
        escritor.println(linhaPalpites)
    }

    // grava o tabuleiro do computador
    escritor.println()
    escritor.println("Computador")
    escritor.println("Real")
    for (linha in tabuleiroComputador.indices) {
        val linhaTabuleiro = tabuleiroComputador[linha].joinToString(",") { it?.toString() ?: "" }
        escritor.println(linhaTabuleiro)
    }

    // grava o tabuleiro de palpites do computador
    escritor.println()
    escritor.println("Computador")
    escritor.println("Palpites")
    for (linha in tabuleiroPalpitesDoComputador.indices) {
        val linhaPalpites = tabuleiroPalpitesDoComputador[linha].joinToString(",") { it?.toString() ?: "" }
        escritor.println(linhaPalpites)
    }

    // fecha o escritor, garantindo que todas as informações sejam salvas no arquivo
    escritor.close()
}

fun calculaEstatisticas(tabuleiroPalpites: Array<Array<Char?>>): Array<Int> {
    var numJogadas = 0
    var numTirosCerteiros = 0
    var numNaviosAfundados = 0

    // Contadores para cada tipo de navio
    val contagemNavios = Array(4) { 0 }

    // Conta os acertos para cada tipo de navio
    for (linha in tabuleiroPalpites) {
        for (celula in linha) {
            if (celula != null) {
                numJogadas++
                if (celula.isDigit()) {
                    numTirosCerteiros++
                    contagemNavios[celula - '1']++
                }
            }
        }
    }

    // Verifica os navios afundados considerando o número de navios de cada tipo
    for (i in contagemNavios.indices) {
        val tamanhoNavio = i + 1
        // Divide o número total de acertos pelo tamanho do navio para obter o número de navios completamente atingidos desse tipo
        numNaviosAfundados += contagemNavios[i] / tamanhoNavio
    }

    return arrayOf(numJogadas, numTirosCerteiros, numNaviosAfundados)
}

fun calculaNaviosFaltaAfundar(tabuleiroPalpites: Array<Array<Char?>>): Array<Int> {
    var portaAvioesRestantes = calculaNumNavios(tabuleiroPalpites.size, tabuleiroPalpites[0].size)[3]
    var naviosTanqueRestantes = calculaNumNavios(tabuleiroPalpites.size, tabuleiroPalpites[0].size)[2]
    var contratorpedeirosRestantes = calculaNumNavios(tabuleiroPalpites.size, tabuleiroPalpites[0].size)[1]
    var submarinosRestantes = calculaNumNavios(tabuleiroPalpites.size, tabuleiroPalpites[0].size)[0]

    val acertos = arrayOf(0, 0, 0, 0) // Contadores para acertos em submarinos, contratorpedeiros, navios-tanque, porta-aviões

    for (linha in tabuleiroPalpites) {
        for (celula in linha) {
            when (celula) {
                '1' -> acertos[0]++
                '2' -> acertos[1]++
                '3' -> acertos[2]++
                '4' -> acertos[3]++
            }
        }
    }

    portaAvioesRestantes -= acertos[3] / 4
    naviosTanqueRestantes -= acertos[2] / 3
    contratorpedeirosRestantes -= acertos[1] / 2
    submarinosRestantes -= acertos[0]

    return arrayOf(max(portaAvioesRestantes, 0), max(naviosTanqueRestantes, 0), max(contratorpedeirosRestantes, 0), max(submarinosRestantes, 0))
}

// função usada para imprimir o tabuleiro de um jogo
fun exibirTabuleiro(tabuleiro: Array<String>) {
    // itera sobre cada linha do array de tabuleiro.
    for (linha in tabuleiro) {
        println(linha)
    }
}

//MENUS DO JOGO

// menu principal
fun menuPrincipal(): Int {
    println("\n> > Batalha Naval < <\n\n1 - Definir Tabuleiro e Navios\n2 - Jogar\n3 - Gravar\n4 - Ler\n0 - Sair\n")

    while (true) {
        when (val resposta = readln().toIntOrNull()) {
            -1 -> return MENU_PRINCIPAL
            1 -> return MENU_DEFINIR_TABULEIRO

            2, 3 -> {
                return if (tabuleiroHumano.isNotEmpty() && tabuleiroComputador.isNotEmpty()) {
                    if (resposta == 2) MENU_JOGAR else MENU_GRAVAR_FICHEIRO
                } else {
                    println("!!! Tem que primeiro definir o tabuleiro do jogo, tente novamente")
                    MENU_PRINCIPAL
                }
            }

            4 -> return MENU_LER_FICHEIRO

            0 -> return SAIR

            else -> println("!!! Opcao invalida, tente novamente")
        }
    }
}

// menu para definir o tamanho do tabuleiro
fun menuDefinirTabuleiro(): Int {
    println("\n> > Batalha Naval < <\n")
    println("Defina o tamanho do tabuleiro:")

    var linhas: Int?
    var colunas: Int?

    // loop infinito até que um tamanho válido de tabuleiro seja fornecido
    while (true) {
        // solicita ao utilizador o número de linhas para o tabuleiro
        println("Quantas linhas?")
        linhas = readlnOrNull()?.toIntOrNull()

        // retorna ao menu principal se o utilizador desejar cancelar
        if (linhas == -1) return MENU_PRINCIPAL

        // solicita ao utilizador o número de colunas para o tabuleiro
        println("Quantas colunas?")
        colunas = readlnOrNull()?.toIntOrNull()

        // retorna ao menu principal se o utilizador desejar cancelar
        if (colunas == -1) return MENU_PRINCIPAL

        // verifica se o número de linhas e colunas é válido
        if (linhas != null && linhas > 0 && colunas != null && colunas > 0) {
            // verifica se o tamanho do tabuleiro é válido de acordo com as regras do jogo
            if (tamanhoTabuleiroValido(linhas, colunas)) {
                // define as dimensões globais do tabuleiro
                numColunas = colunas
                numLinhas = linhas

                // cria tabuleiros vazios para o humano e para o computador, incluindo os tabuleiros de palpites
                tabuleiroHumano = criaTabuleiroVazio(numLinhas, numColunas)
                tabuleiroComputador = criaTabuleiroVazio(numLinhas, numColunas)
                tabuleiroPalpitesDoHumano = criaTabuleiroVazio(numLinhas, numColunas)
                tabuleiroPalpitesDoComputador = criaTabuleiroVazio(numLinhas, numColunas)

                // exibe o tabuleiro vazio para o utilizador
                exibirTabuleiro(obtemMapa(tabuleiroHumano, true))

                // retorna o código para o próximo menu que é o de definição dos navios
                return MENU_DEFINIR_NAVIOS
            } else {
                // informa o utilizador se o tamanho do tabuleiro for inválido
                println("!!! Tamanho do tabuleiro inválido, tente novamente")
            }
        } else {
            // informa o utilizador se os números fornecidos forem inválidos
            println("!!! Número de linhas e colunas inválidas, tente novamente")
        }
    }
}

// menu para definir a posição dos navios no tabuleiro
fun menuDefinirNavios(): Int {
    // calcula o número de navios de cada tamanho com base no tamanho do tabuleiro
    val numerodeNavios = calculaNumNavios(numLinhas, numColunas)

    // itera sobre cada tamanho de navio
    for (tamanho in numerodeNavios.indices) {
        // define quantos navios desse tamanho precisam ser posicionados
        var naviosParaPosicionar = numerodeNavios[tamanho]

        // continua a posicionar navios enquanto houver navios para posicionar
        while (naviosParaPosicionar > 0) {
            // exibe a mensagem solicitando as coordenadas do navio, varia conforme o tamanho
            val mensagem = when (tamanho) {
                0 -> "Insira as coordenadas de um submarino:"
                1 -> "Insira as coordenadas de um contra-torpedeiro:"
                2 -> "Insira as coordenadas de um navio-tanque:"
                else -> "Insira as coordenadas de um porta avioes:"
            }
            println(mensagem)

            // solicita ao utilizador as coordenadas e a orientação do navio
            val (coordenadas, orientacao) = obterCoordenadasValidas(numLinhas, numColunas, tamanho + 1)
            if (coordenadas == null) return MENU_PRINCIPAL // retorna ao menu principal se o utilizador cancelar a operação.

            // insere o navio no tabuleiro com base nas coordenadas e orientação fornecidas
            val navioInserido = if (orientacao != null) {
                insereNavio(
                    tabuleiroHumano,
                    coordenadas.first,
                    coordenadas.second,
                    orientacao,
                    tamanho + 1
                ) // se me pedirem para alterar tamanhos navios
            } else {
                insereNavioSimples(tabuleiroHumano, coordenadas.first, coordenadas.second, 1)
            }

            // verifica se o navio foi inserido com sucesso
            if (navioInserido) {
                // mostra o tabuleiro atualizado
                exibirTabuleiro(obtemMapa(tabuleiroHumano, true))
                naviosParaPosicionar--
            } else {
                // informa o utilizador que o posicionamento foi inválido e solicita nova tentativa
                println("!!! Posicionamento invalido, tente novamente")
            }
        }
    }

    // preenche o tabuleiro do computador automaticamente
    preencheTabuleiroComputador(tabuleiroComputador, calculaNumNavios(numLinhas, numColunas))

    // pergunta ao utilizador se deseja visualizar o tabuleiro do computador
    println("Pretende ver o mapa gerado para o Computador? (S/N)")
    if (readlnOrNull() == "S") {
        exibirTabuleiro(obtemMapa(tabuleiroComputador, true))
    }

    // retorna ao menu principal.
    return MENU_PRINCIPAL
}

// menu para a fase de jogo
fun menuJogar(): Int {
    while (true) {
        // mostra o tabuleiro de palpites do humano
        exibirTabuleiro(obtemMapa(tabuleiroPalpitesDoHumano, false))

        var coordenadas: Pair<Int, Int>? = null
        // loop para obter coordenadas válidas do utilizador
        while (coordenadas == null) {
            println("Indique a posição que pretende atingir")
            println("Coordenadas? (ex: 6,G)")

            val input = readlnOrNull()?.trim().toString()

            if (input.toIntOrNull() == -1) {
                return MENU_PRINCIPAL
            } else if (input == "?") {
                val naviosRestantes = calculaNaviosFaltaAfundar(tabuleiroPalpitesDoHumano)
                val tiposNavios = arrayOf("porta-avião(s)", "navio-tanque(s)", "contra-torpedeiro(s)", "submarino(s)")
                val mensagem = StringBuilder("Falta afundar: ")

                for (i in naviosRestantes.indices) {
                    if (naviosRestantes[i] > 0) {
                        mensagem.append("${naviosRestantes[i]} ${tiposNavios[i]}")
                        if (i < naviosRestantes.size - 1) {
                            mensagem.append("; ")
                        }
                    }
                }

                if (mensagem.length > "Falta afundar: ".length) {
                    println(mensagem.toString())
                }
            } else {
                coordenadas = processaCoordenadas(input, numLinhas, numColunas)
            }
        }

        // O utilizador realiza um tiro e o resultado é impresso
        print(">>> HUMANO >>>" + lancarTiro(tabuleiroComputador, tabuleiroPalpitesDoHumano, coordenadas))
        // verifica se um navio foi completamente atingido
        if (navioCompleto(tabuleiroPalpitesDoHumano, coordenadas.first, coordenadas.second)) {
            println(" Navio ao fundo!")
        } else {
            println()
        }

        // verifica se o utilizador venceu o jogo
        if (venceu(tabuleiroPalpitesDoHumano)) {
            println("PARABENS! Venceu o jogo!")
            println("Prima enter para voltar ao menu principal")
            readlnOrNull()
            return MENU_PRINCIPAL
        }

        // gera um tiro aleatório para o computador
        val tiroComputador = geraTiroComputador(tabuleiroPalpitesDoComputador)
        val letraColuna = (tiroComputador.second - 1 + 'A'.toInt()).toChar()
        println("Computador lançou tiro para a posição (${tiroComputador.first},${letraColuna})")
        // processa o tiro do computador e exibe o resultado
        val elementoTabuleiro = tabuleiroHumano[tiroComputador.first - 1][tiroComputador.second - 1]
        when (elementoTabuleiro) {
            null -> print(">>> COMPUTADOR >>> Agua.")
            '1' -> print(">>> COMPUTADOR >>> Tiro num submarino.")
            '2' -> print(">>> COMPUTADOR >>> Tiro num contra-torpedeiro.")
            '3' -> print(">>> COMPUTADOR >>> Tiro num navio-tanque.")
            '4' -> print(">>> COMPUTADOR >>> Tiro num porta-aviões.")
        }
        // verifica se o computador atingiu completamente um navio
        if (navioCompleto(tabuleiroPalpitesDoComputador, tiroComputador.first, tiroComputador.second)) {
            println(" Navio ao fundo!")
        } else {
            println()
        }

        // verifica se o computador venceu o jogo.
        if (venceu(tabuleiroPalpitesDoComputador)) {
            println("OPS! O computador venceu o jogo!")
            println("Prima enter para voltar ao menu principal")
            readlnOrNull()
            return MENU_PRINCIPAL
        }

        // Pausa antes de continuar o jogo.
        println("Prima enter para continuar")
        readlnOrNull()
    }
}

// menu que permite ao utilizador guardar o estado atual do jogo
fun menuGravarFicheiro(): Int {
    // solicita ao utilizador que insira o nome do arquivo onde o jogo será salvo
    println("Introduza o nome do ficheiro (ex: jogo.txt)")

    // lê o nome do arquivo do utilizador
    val fileName = readlnOrNull().toString()

    // chama a função para gravar o jogo, passando o nome do arquivo e os tabuleiros do jogo
    gravarJogo(
        fileName,
        tabuleiroHumano,
        tabuleiroPalpitesDoHumano,
        tabuleiroComputador,
        tabuleiroPalpitesDoComputador
    )

    // informa o utilizador que o jogo foi salvo com sucesso.
    println("Tabuleiro ${numLinhas}x${numColunas} gravado com sucesso")

    // retorna ao menu principal após a gravação do arquivo.
    return MENU_PRINCIPAL
}

// menu para carregar o estado de um jogo
fun menuLerFicheiro(): Int {
    // solicita ao utilizador que insira o nome do arquivo de onde o jogo será carregado
    println("Introduza o nome do ficheiro (ex: jogo.txt)")

    // lê o nome do arquivo fornecido pelo utilizador. Retorna ao menu principal se nenhuma entrada for fornecida
    val nomeFicheiro = readlnOrNull() ?: return MENU_PRINCIPAL

    // cria um array para armazenar os quatro tabuleiros (dois reais e dois de palpites)
    val tabuleiros = arrayOfNulls<Array<Array<Char?>>>(4)

    // carrega cada um dos quatro tabuleiros do arquivo
    for (i in 1..4) {
        tabuleiros[i - 1] = lerJogo(nomeFicheiro, i)
    }

    // atribui os tabuleiros carregados às variáveis globais correspondentes
    tabuleiroHumano = tabuleiros[0] ?: arrayOf()
    tabuleiroPalpitesDoHumano = tabuleiros[1] ?: arrayOf()
    tabuleiroComputador = tabuleiros[2] ?: arrayOf()
    tabuleiroPalpitesDoComputador = tabuleiros[3] ?: arrayOf()

    // informa o utilizador que o tabuleiro foi carregado com sucesso.
    println("Tabuleiro ${numLinhas}x${numColunas} lido com sucesso")

    // exibe o tabuleiro humano para o utilizador
    exibirTabuleiro(obtemMapa(tabuleiroHumano, true))

    // retorna ao menu principal.
    return MENU_PRINCIPAL
}

fun main() {
    var menuActual = MENU_PRINCIPAL

    while (true) {

        menuActual = when (menuActual) {
            MENU_PRINCIPAL -> menuPrincipal()
            MENU_DEFINIR_TABULEIRO -> menuDefinirTabuleiro()
            MENU_DEFINIR_NAVIOS -> menuDefinirNavios()
            MENU_JOGAR -> menuJogar()
            MENU_LER_FICHEIRO -> menuLerFicheiro()
            MENU_GRAVAR_FICHEIRO -> menuGravarFicheiro()
            SAIR -> return
            else -> return

        }
    }
}
