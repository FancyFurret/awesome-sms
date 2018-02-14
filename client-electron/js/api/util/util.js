function randomColor() {

    let colors = [
        materialColors.red["500"],
        materialColors.pink["500"],
        materialColors.purple["500"],
        materialColors.deepPurple["500"],
        materialColors.indigo["500"],
        materialColors.blue["500"],
        materialColors.lightBlue["500"],
        materialColors.cyan["500"],
        materialColors.teal["500"],
        materialColors.green["500"],
        materialColors.lightGreen["500"],
        materialColors.lime["500"],
        materialColors.yellow["500"],
        materialColors.amber["500"],
        materialColors.orange["500"],
        materialColors.deepOrange["500"],
        materialColors.brown["500"],
        materialColors.grey["500"],
        materialColors.blueGrey["500"]
    ];

    return colors[Math.floor(Math.random() * colors.length)];

    // let colors = Object.keys(materialColors[color_name]);
    // return materialColors[color_name][colors[Math.floor(Math.random() * colors.length)]];
}