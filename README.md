/* =========================================================================
   CAMADA DE INTEGRAÇÃO COM O BACKEND JAVA
   -------------------------------------------------------------------------
   Todo o front-end fala apenas com o objeto FunCupAPI abaixo. Hoje ele
   devolve dados de exemplo (mock), pensados para bater exatamente com a
   estrutura que o backend Java (ex: Spring Boot) deve retornar.

   Quando o back-end estiver pronto, basta trocar o corpo de cada função
   pelo fetch() comentado logo acima dela — o resto do dashboard (telas,
   renderização, filtros) não precisa mudar em nada.

   Sugestão de endpoints REST:
     GET  /api/jogos                 -> Jogo[]
     GET  /api/grupos                -> { [letra: string]: Selecao[] }
     GET  /api/jogadores             -> Jogador[]
     GET  /api/jogadores/destaques   -> Jogador[] (top artilheiros)
     GET  /api/selecoes              -> SelecaoResumo[]
     POST /api/ia/perguntar          -> { pergunta: string } => { resposta: string }
   ========================================================================= */

const API_BASE_URL = "http://localhost:8080"; // ajuste para a URL do seu backend Java

const FunCupAPI = {

  async getEstatisticasGerais() {
    // TODO (Java): GET /api/estatisticas
    // const resp = await fetch(`${API_BASE_URL}/api/estatisticas`);
    // return await resp.json();
    return { jogosRealizados: 48, selecoes: 32, golsMarcados: 134, jogadores: 512 };
  },

  async getGrupos() {
    // TODO (Java): GET /api/grupos
    // const resp = await fetch(`${API_BASE_URL}/api/grupos`);
    // return await resp.json();
    return MOCK_GRUPOS;
  },

  async getJogos() {
    // TODO (Java): GET /api/jogos
    // const resp = await fetch(`${API_BASE_URL}/api/jogos`);
    // return await resp.json();
    return MOCK_JOGOS;
  },

  async getJogadores() {
    // TODO (Java): GET /api/jogadores
    // const resp = await fetch(`${API_BASE_URL}/api/jogadores`);
    // return await resp.json();
    return MOCK_JOGADORES;
  },

  async getJogadoresDestaque() {
    // TODO (Java): GET /api/jogadores/destaques
    // const resp = await fetch(`${API_BASE_URL}/api/jogadores/destaques`);
    // return await resp.json();
    return MOCK_JOGADORES.slice().sort((a, b) => b.gols - a.gols).slice(0, 4);
  },

  async getSelecoes() {
    // TODO (Java): GET /api/selecoes
    // const resp = await fetch(`${API_BASE_URL}/api/selecoes`);
    // return await resp.json();
    return MOCK_SELECOES;
  },

  async perguntarIA(pergunta) {
    // TODO (Java): POST /api/ia/perguntar  body: { pergunta }
    // const resp = await fetch(`${API_BASE_URL}/api/ia/perguntar`, {
    //   method: "POST",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify({ pergunta })
    // });
    // const dados = await resp.json();
    // return dados.resposta;

    // Resposta simulada apenas para o front-end funcionar isoladamente:
    await esperar(900);
    return "Essa é uma resposta de exemplo. Conecte o endpoint /api/ia/perguntar no seu backend Java para respostas reais.";
  }
};

function esperar(ms){ return new Promise(res => setTimeout(res, ms)); }
