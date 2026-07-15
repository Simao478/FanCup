/* ============================ NAVEGAÇÃO ================================ */

document.querySelectorAll(".nav-item").forEach(item => {
  item.addEventListener("click", () => {
    document.querySelectorAll(".nav-item").forEach(i => i.classList.remove("active"));
    document.querySelectorAll(".page").forEach(p => p.classList.remove("active"));
    item.classList.add("active");
    document.getElementById("page-" + item.dataset.page).classList.add("active");
  });
});


/* ============================ RENDER: INÍCIO ============================ */

async function carregarInicio() {
  const stats = await FunCupAPI.getEstatisticasGerais();
  document.querySelector("#page-inicio .stats-grid").innerHTML = `
    <div class="stat-card"><div class="num">${stats.jogosRealizados}</div><div class="label">Jogos realizados</div></div>
    <div class="stat-card"><div class="num">${stats.selecoes}</div><div class="label">Seleções</div></div>
    <div class="stat-card"><div class="num">${stats.golsMarcados}</div><div class="label">Gols marcados</div></div>
    <div class="stat-card"><div class="num">${stats.jogadores}</div><div class="label">Jogadores</div></div>
  `;

  const grupos = await FunCupAPI.getGrupos();
  renderTabelaGrupo(document.getElementById("tbl-grupo-a"), grupos["A"]);

  const destaques = await FunCupAPI.getJogadoresDestaque();
  document.getElementById("lista-destaques").innerHTML = destaques.map(rowJogador).join("");
}

function renderTabelaGrupo(tbody, selecoes) {
  tbody.innerHTML = selecoes.map(s => `
    <tr>
      <td class="${s.classificada ? "classificado" : ""}">${s.nome}</td>
      <td>${s.pj}</td>
      <td>${s.v}</td>
      <td>${s.d}</td>
      <td>${s.sg > 0 ? "+" + s.sg : s.sg}</td>
      <td>${s.pts}</td>
    </tr>
  `).join("");
}

function rowJogador(j) {
  return `
    <div class="player-row">
      <div class="badge">${j.sigla}</div>
      <div class="info">
        <div class="name">${j.nome}</div>
        <div class="sub">${j.selecao} · ${j.clube}</div>
      </div>
      <div class="goals">${j.gols}</div>
    </div>
  `;
}


/* ============================ RENDER: JOGOS ============================ */

let jogosCache = [];

async function carregarJogos() {
  const cont = document.getElementById("lista-jogos");
  cont.innerHTML = skeletonRows(4);

  jogosCache = await FunCupAPI.getJogos();
  renderJogos(jogosCache);
}

function renderJogos(lista) {
  const cont = document.getElementById("lista-jogos");
  if (!lista.length) {
    cont.innerHTML = `<div class="empty-state">Nenhum jogo encontrado.</div>`;
    return;
  }
  cont.innerHTML = lista.map(j => {
    const placar = j.status === "encerrado" || j.placarA !== null
      ? `${j.placarA} - ${j.placarB}`
      : "vs";
    const statusLabel = { encerrado:"Encerrado", aovivo:"Ao vivo", agendado:"Agendado" }[j.status];
    return `
      <div class="jogo-card">
        <div>
          <div class="jogo-teams">${j.selecaoA} <span class="jogo-placar">${placar}</span> ${j.selecaoB}</div>
          <div class="jogo-meta">Grupo ${j.grupo}</div>
        </div>
        <span class="status-pill status-${j.status}">${statusLabel}</span>
      </div>
    `;
  }).join("");
}

document.getElementById("filtroStatusJogos").addEventListener("change", (e) => {
  const valor = e.target.value;
  const filtrados = valor === "todos" ? jogosCache : jogosCache.filter(j => j.status === valor);
  renderJogos(filtrados);
});


/* ============================ RENDER: GRUPOS ============================ */

async function carregarGrupos() {
  const grupos = await FunCupAPI.getGrupos();
  const seletor = document.getElementById("seletorGrupo");
  seletor.innerHTML = Object.keys(grupos).map(letra => `<option value="${letra}">Grupo ${letra}</option>`).join("");

  const renderizarSelecionado = () => renderTabelaGrupo(document.getElementById("tbl-grupo-selecionado"), grupos[seletor.value]);
  seletor.addEventListener("change", renderizarSelecionado);
  renderizarSelecionado();
}


/* ============================ RENDER: JOGADORES ============================ */

let jogadoresCache = [];

async function carregarJogadores() {
  const cont = document.getElementById("lista-jogadores");
  cont.innerHTML = skeletonRows(4);

  jogadoresCache = await FunCupAPI.getJogadores();
  renderJogadores(jogadoresCache);
}

function renderJogadores(lista) {
  const cont = document.getElementById("lista-jogadores");
  if (!lista.length) {
    cont.innerHTML = `<div class="empty-state">Nenhum jogador encontrado.</div>`;
    return;
  }
  cont.innerHTML = lista.map(rowJogador).join("");
}

document.getElementById("buscaJogador").addEventListener("input", (e) => {
  const termo = e.target.value.toLowerCase();
  const filtrados = jogadoresCache.filter(j =>
    j.nome.toLowerCase().includes(termo) || j.selecao.toLowerCase().includes(termo)
  );
  renderJogadores(filtrados);
});


/* ============================ RENDER: SELEÇÕES ============================ */

async function carregarSelecoes() {
  const grid = document.getElementById("grid-selecoes");
  grid.innerHTML = skeletonCards(8);

  const selecoes = await FunCupAPI.getSelecoes();
  grid.innerHTML = selecoes.map(s => `
    <div class="selecao-card">
      <div class="flag">${s.bandeira}</div>
      <div class="nome">${s.nome}</div>
      <div class="grupo">Grupo ${s.grupo}</div>
    </div>
  `).join("");
}


/* ============================ HELPERS DE LOADING ============================ */

function skeletonRows(qtd) {
  return Array.from({ length: qtd }).map(() =>
    `<div class="skeleton" style="height:52px;margin-bottom:10px;"></div>`
  ).join("");
}

function skeletonCards(qtd) {
  return Array.from({ length: qtd }).map(() =>
    `<div class="skeleton" style="height:110px;"></div>`
  ).join("");
}


/* ============================ IA ESPORTIVA (CHAT) ============================ */

const iaMensagens = document.getElementById("iaMensagens");
const iaInput = document.getElementById("iaInput");
const iaEnviarBtn = document.getElementById("iaEnviar");

function adicionarBolha(texto, autor) {
  const row = document.createElement("div");
  row.className = `bubble-row ${autor}`;
  row.innerHTML = `<div class="bubble ${autor}">${texto}</div>`;
  iaMensagens.appendChild(row);
  iaMensagens.scrollTop = iaMensagens.scrollHeight;
  return row;
}

async function enviarPerguntaIA() {
  const pergunta = iaInput.value.trim();
  if (!pergunta) return;

  adicionarBolha(pergunta, "user");
  iaInput.value = "";

  const typingRow = document.createElement("div");
  typingRow.className = "bubble-row ia";
  typingRow.innerHTML = `<div class="bubble ia typing">digitando...</div>`;
  iaMensagens.appendChild(typingRow);
  iaMensagens.scrollTop = iaMensagens.scrollHeight;

  const resposta = await FunCupAPI.perguntarIA(pergunta);

  typingRow.remove();
  adicionarBolha(resposta, "ia");
}

iaEnviarBtn.addEventListener("click", enviarPerguntaIA);
iaInput.addEventListener("keydown", (e) => { if (e.key === "Enter") enviarPerguntaIA(); });

adicionarBolha("👋 Olá! Sou a FUNCUP IA. Estou pronta para responder qualquer pergunta sobre o mercado de apostas Futebolístico.", "ia");


/* ============================ INICIALIZAÇÃO ============================ */

carregarInicio();
carregarJogos();
carregarGrupos();
carregarJogadores();
carregarSelecoes();
