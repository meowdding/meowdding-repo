import fs from "fs";


const entries = [
    {
        source: "constants/animatedskulls.json",
        destination: "skyocean/skulls.json",
        handler: (skulls) => {
            let skins = skulls["skins"]
            for (let key in skins) {
                let skin = skins[key]
                if (skin["textures"].length === 1) {
                    skin.ticks = undefined
                }
            }
            return skins
        }
    },
    {
        source: "constants/dyes.json",
        destination: "skyocean/dyes.json",
    },
    {
        source: "constants/bestiary.json",
        destination: "neu/bestiary.json",
    },
    {
        source: "constants/misc.json",
        destination: "neu/misc.json",
    }
]

async function update_misc() {
    let bestiary = await fetch("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/misc.json")
        .then((x) => x.json())

    fs.writeFileSync(`repo/neu/misc.json`, JSON.stringify(bestiary, null, 2));
}

async function main() {
    for (let entry of entries) {
        let source = entry.source
        let destination = entry.destination
        let handler = entry.handler || ((x) => { return x })

        console.log(`Fetching ${source}`)
        let result = await fetch(`https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/${source}`)
            .then((x) => x.json())

        console.log(`Writing ${destination}`)
        fs.writeFileSync(`repo/${destination}`, JSON.stringify(handler(result), null, 2));
    }
}

await main()
