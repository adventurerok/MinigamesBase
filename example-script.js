
//for example, this could be for the Lightning Wand. rightClick would only be called for right click events.
function rightClick() {
    target = user.rayTrace({range: 200});

    if(target) {
        Item.startCooldown();
        World.strikeLightning(target, {
            representing: user,
            attackUser: function(e) {
                e.damage *= lightningMultiplier(user);
            }
        });
    }


}