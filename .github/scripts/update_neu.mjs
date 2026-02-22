import fs from "fs";

async function update_skulls() {
    let skulls = await fetch("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/animatedskulls.json")
        .then((x) => x.json())

    let skins = skulls["skins"]
    for (let key in skins) {
        let skin = skins[key]
        if (skin["textures"].length == 1) {
            skin.ticks = undefined
        }
    }

    fs.writeFileSync(`repo/skyocean/skulls.json`, JSON.stringify(skins, null, 2));
}

async function update_dyes() {
    let dyes = await fetch("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/dyes.json")
        .then((x) => x.json())

    fs.writeFileSync(`repo/skyocean/dyes.json`, JSON.stringify(dyes, null, 2));
}

async function update_bestiary() {
    let bestiary = await fetch("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/bestiary.json")
        .then((x) => x.json())

    fs.writeFileSync(`repo/neu/bestiary.json`, JSON.stringify(bestiary, null, 2));
}
async function update_misc() {
    let bestiary = await fetch("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/misc.json")
        .then((x) => x.json())

    fs.writeFileSync(`repo/neu/misc.json`, JSON.stringify(bestiary, null, 2));
}

async function main() {
    await update_dyes()
    await update_skulls()
    await update_misc()
    await update_bestiary()
}

await main()
