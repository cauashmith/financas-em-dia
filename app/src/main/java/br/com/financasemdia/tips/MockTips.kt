package br.com.financasemdia.tips

object MockTips {

    val all: List<FinancialTip> = listOf(
        FinancialTip(
            id = 1,
            title = "Regra 50-30-20",
            summary = "Divida sua renda entre necessidades, desejos e futuro.",
            content = "Uma forma simples de organizar o orçamento é destinar 50% da renda líquida às " +
                "necessidades (moradia, alimentação, transporte), 30% aos desejos (lazer, assinaturas) " +
                "e 20% para objetivos futuros, como reserva de emergência e investimentos.",
            category = TipCategory.BUDGET,
            steps = listOf(
                "Calcule sua renda líquida mensal",
                "Liste seus gastos fixos essenciais",
                "Defina um limite mensal para lazer",
                "Separe 20% assim que receber o salário",
            ),
            readingMinutes = 3,
            source = "Educação financeira — conceito popularizado por Elizabeth Warren",
        ),
        FinancialTip(
            id = 2,
            title = "Monte sua reserva de emergência",
            summary = "Tenha de 3 a 6 meses de custo de vida guardados.",
            content = "A reserva de emergência protege você de imprevistos como desemprego ou problemas " +
                "de saúde. O ideal é mantê-la em uma aplicação de baixo risco e com liquidez diária.",
            category = TipCategory.SAVINGS,
            steps = listOf(
                "Some seus gastos mensais essenciais",
                "Multiplique por 6 para definir a meta",
                "Escolha uma aplicação com liquidez diária",
                "Programe um depósito automático mensal",
            ),
            readingMinutes = 4,
        ),
        FinancialTip(
            id = 3,
            title = "Planeje as compras do mercado",
            summary = "Lista de compras e cardápio semanal reduzem desperdício.",
            content = "Ir ao mercado sem lista aumenta as compras por impulso. Planejar as refeições da " +
                "semana ajuda a comprar só o necessário e a aproveitar melhor os alimentos.",
            category = TipCategory.FOOD,
            steps = listOf(
                "Monte um cardápio para a semana",
                "Confira o que já existe na despensa",
                "Faça a lista e siga-a no mercado",
                "Compare o preço por quilo ou litro",
            ),
            readingMinutes = 2,
        ),
        FinancialTip(
            id = 4,
            title = "Reduza a conta de energia",
            summary = "Pequenos hábitos fazem diferença no fim do mês.",
            content = "Aparelhos em modo de espera, banhos longos e lâmpadas antigas pesam na conta. " +
                "Trocar por lâmpadas LED e desligar aparelhos da tomada reduz o consumo.",
            category = TipCategory.BILLS,
            steps = listOf(
                "Troque lâmpadas antigas por LED",
                "Desligue aparelhos em modo de espera",
                "Reduza o tempo de banho",
            ),
            source = "Recomendações gerais de eficiência energética",
        ),
        FinancialTip(
            id = 5,
            title = "Saia do rotativo do cartão",
            summary = "Os juros do rotativo estão entre os mais altos do mercado.",
            content = "Se não for possível pagar a fatura inteira, procure trocar a dívida por uma linha " +
                "de crédito mais barata e negocie diretamente com o banco.",
            category = TipCategory.DEBT,
            steps = listOf(
                "Liste todas as dívidas e suas taxas de juros",
                "Priorize quitar as de juros mais altos",
                "Negocie condições com o banco",
                "Evite novas compras parceladas até quitar",
            ),
            readingMinutes = 5,
        ),
        FinancialTip(
            id = 6,
            title = "Revise o custo com transporte",
            summary = "Compare aplicativo, transporte público e carro próprio.",
            content = "O custo real do carro inclui combustível, seguro, IPVA, manutenção e " +
                "estacionamento. Colocar tudo na ponta do lápis ajuda a escolher a melhor opção.",
            category = TipCategory.TRANSPORT,
            steps = listOf(
                "Registre todos os gastos com transporte por um mês",
                "Compare com o custo de alternativas",
                "Considere caronas ou bicicleta em trajetos curtos",
            ),
        ),
        FinancialTip(
            id = 7,
            title = "Lazer cabe no orçamento",
            summary = "Defina um valor fixo para diversão e aproveite sem culpa.",
            content = "Cortar todo o lazer costuma não durar. Definir um teto mensal permite se divertir " +
                "mantendo as contas em dia. Procure também eventos gratuitos na sua cidade.",
            category = TipCategory.LEISURE,
            steps = listOf(
                "Defina um teto mensal para lazer",
                "Pesquise eventos gratuitos",
                "Revise assinaturas pouco usadas",
            ),
            readingMinutes = 2,
        ),
        FinancialTip(
            id = 8,
            title = "Negocie o aluguel e as contas da casa",
            summary = "Moradia costuma ser o maior gasto do mês.",
            content = "Na renovação do contrato, pesquise imóveis semelhantes e use os dados para negociar. " +
                "Revise também planos de internet e telefone, que mudam com frequência.",
            category = TipCategory.HOUSING,
            steps = listOf(
                "Pesquise o valor de imóveis semelhantes",
                "Proponha um reajuste menor na renovação",
                "Revise os planos de internet e telefone",
            ),
            readingMinutes = 3,
        ),
    )

    fun findById(id: Int): FinancialTip? = all.firstOrNull { it.id == id }
}
