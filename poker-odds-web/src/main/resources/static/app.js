'use strict';

// cards use core notation: As, Td, 7c

const RANKS = ['A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2'];
const SUITS = ['s', 'h', 'd', 'c'];
const SUIT_SYMBOLS = { s: '♠', h: '♥', d: '♦', c: '♣' };
const RED_SUITS = ['h', 'd'];

// slots in fill order: hand, flop, turn, river

const SLOT_LABELS = ['Card 1', 'Card 2', 'Flop', 'Flop', 'Flop', 'Turn', 'River'];
const HAND = [0, 1];
const FLOP = [2, 3, 4];
const TURN = 5;
const RIVER = 6;

// card per slot, null if empty
let slots = new Array(SLOT_LABELS.length).fill(null);

// lets us ignore answers to old requests
let requestId = 0;

// --- helpers ---

// "Th" -> "T♥"
function cardText(code) {
    return code[0] + SUIT_SYMBOLS[code[1]];
}

function colorClass(code) {
    return RED_SUITS.includes(code[1]) ? 'red' : 'black';
}

// tiny createElement wrapper
function el(tag, className, text) {
    const element = document.createElement(tag);
    if (className) {
        element.className = className;
    }
    if (text !== undefined) {
        element.textContent = text;
    }
    return element;
}

function formatPercent(probability) {
    return (probability * 100).toFixed(2) + '%';
}

// --- slot rules ---

function isFilled(index) {
    return slots[index] !== null;
}

// turn needs the full flop, river needs the turn
function isAvailable(index) {
    if (index === TURN) {
        return FLOP.every(isFilled);
    }
    if (index === RIVER) {
        return isFilled(TURN);
    }
    return true;
}

// next free slot, or -1
function nextEmptySlot() {
    return slots.findIndex((card, index) => card === null && isAvailable(index));
}

function isUsed(code) {
    return slots.includes(code);
}

// board cards in order
function boardCards() {
    return [...FLOP, TURN, RIVER].map(index => slots[index]).filter(card => card !== null);
}

// need hand + flop before asking the server
function isReadyToAnalyze() {
    return HAND.every(isFilled) && FLOP.every(isFilled);
}

// --- user actions ---

function pickCard(code) {
    const index = nextEmptySlot();
    if (index === -1 || isUsed(code)) {
        return;
    }
    slots[index] = code;
    update();
}

// removing a flop card also clears turn and river, removing the turn clears the river
function removeCard(index) {
    slots[index] = null;
    if (FLOP.includes(index)) {
        slots[TURN] = null;
        slots[RIVER] = null;
    } else if (index === TURN) {
        slots[RIVER] = null;
    }
    update();
}

function newHand() {
    slots = slots.map(() => null);
    update();
}

// --- left column ---

function renderSlot(index) {
    const code = slots[index];
    const slot = el('div', 'slot');
    const button = el('button', 'slot-card');
    button.type = 'button';

    if (code !== null) {
        button.classList.add('filled', colorClass(code));
        button.textContent = cardText(code);
        button.title = 'Click to remove';
        button.addEventListener('click', () => removeCard(index));
    } else {
        button.disabled = !isAvailable(index);
        if (index === nextEmptySlot()) {
            button.classList.add('next');
        }
    }

    slot.append(button, el('span', 'slot-label', SLOT_LABELS[index]));
    return slot;
}

function renderSlots() {
    document.getElementById('hand-slots').replaceChildren(...HAND.map(renderSlot));
    document.getElementById('board-slots').replaceChildren(...[...FLOP, TURN, RIVER].map(renderSlot));
}

// 4 rows (suits) x 13 ranks
function renderPicker() {
    const noFreeSlot = nextEmptySlot() === -1;
    const buttons = [];
    for (const suit of SUITS) {
        for (const rank of RANKS) {
            const code = rank + suit;
            const button = el('button', 'pick ' + colorClass(code), cardText(code));
            button.type = 'button';
            button.disabled = isUsed(code) || noFreeSlot;
            button.addEventListener('click', () => pickCard(code));
            buttons.push(button);
        }
    }
    document.getElementById('picker').replaceChildren(...buttons);
}

// --- results ---

function showError(message) {
    const box = document.getElementById('error');
    box.textContent = message;
    box.hidden = false;
}

function hideError() {
    document.getElementById('error').hidden = true;
}

function showResults(...children) {
    document.getElementById('results').replaceChildren(...children);
}

function stat(label, value, note) {
    const box = el('div', 'stat');
    box.append(el('div', 'stat-label', label), el('div', 'stat-value', value));
    if (note) {
        box.append(el('div', 'stat-note', note));
    }
    return box;
}

function cardList(codes) {
    const list = el('div', 'card-list');
    list.append(...codes.map(code => el('span', 'mini-card ' + colorClass(code), cardText(code))));
    return list;
}

// outs grouped by combination
function renderOuts(outs) {
    const section = el('div');
    section.append(el('h2', null, 'Outs'));
    if (outs.length === 0) {
        section.append(el('p', 'hint', 'No card improves your hand.'));
        return section;
    }
    for (const group of outs) {
        const block = el('div', 'outs-group');
        block.append(el('h3', null, `${group.combination} (${group.cards.length})`), cardList(group.cards));
        section.append(block);
    }
    return section;
}

// by-river table (flop only)
function renderByRiverTable(byRiver) {
    const section = el('div');
    section.append(el('h2', null, 'By the river'));
    const table = el('table');
    const header = el('tr');
    header.append(el('th', null, 'Combination'), el('th', 'number', 'Probability'));
    table.append(header);
    for (const row of byRiver) {
        const tr = el('tr');
        tr.append(el('td', null, row.combination), el('td', 'number', formatPercent(row.probability)));
        table.append(tr);
    }
    section.append(table);
    return section;
}

function renderFlopOrTurn(result) {
    const isFlop = result.street === 'FLOP';
    const stats = el('div', 'stats');
    stats.append(
        stat('Outs', String(result.outsCount), `of ${result.unseenCards} unseen`),
        stat(isFlop ? 'Next card (turn)' : 'Next card (river)', formatPercent(result.nextCardProbability)));
    if (isFlop) {
        stats.append(
            stat('By the river', formatPercent(result.byRiverProbability), 'turn or river'),
            stat('Runner-runner', formatPercent(result.runnerRunnerProbability), 'needs both cards'));
    }

    const parts = [
        el('div', 'street', result.street),
        el('div', 'combination', result.combination),
        stats,
        renderOuts(result.outs),
    ];
    if (isFlop && result.byRiver.length > 0) {
        parts.push(renderByRiverTable(result.byRiver));
    }
    showResults(...parts);
}

function renderRiver(result) {
    showResults(
        el('div', 'street', 'RIVER'),
        el('div', 'combination large', result.combination));
}

// --- server ---

// throws with the server's error message if the request fails
async function requestAnalysis(hole, board) {
    const response = await fetch('/api/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ hole, board }),
    });
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body && body.error ? body.error : `Request failed (HTTP ${response.status})`);
    }
    return body;
}

async function analyze() {
    const id = ++requestId;
    hideError();
    if (!isReadyToAnalyze()) {
        showResults(el('p', 'hint', 'Choose 2 hand cards and 3 flop cards.'));
        return;
    }

    try {
        const result = await requestAnalysis(HAND.map(index => slots[index]), boardCards());
        if (id !== requestId) {
            return;   // stale answer
        }
        if (result.street === 'RIVER') {
            renderRiver(result);
        } else {
            renderFlopOrTurn(result);
        }
    } catch (error) {
        if (id === requestId) {
            showResults();
            showError(error.message);
        }
    }
}

// redraw and re-analyze after every change

function update() {
    renderSlots();
    renderPicker();
    analyze();
}

document.getElementById('new-hand').addEventListener('click', newHand);
update();
