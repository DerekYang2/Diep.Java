public class TankBuilds {
    // Static creation methods
    public static BarrelManager tank() {
        Turret[] turrets = new Turret[]{
                new Turret(42, 95, 0, 0)
        };
        ShootManager shootManager = new ShootManager(new int[]{0}, new float[]{0});
        return new BarrelManager(turrets, shootManager);
    }

    // Triplet
    public static BarrelManager triplet() {
        Turret[] turrets = new Turret[]{
                new Turret(42, 80, 26, 0),
                new Turret(42, 80, -26, 0),
                new Turret(42, 95, 0, 0)
        };
        ShootManager shootManager = new ShootManager(new int[]{1, 1, 0}, new float[]{0, 0.5f});
        return new BarrelManager(turrets, shootManager);
    }

    // Pentashot
    public static BarrelManager pentashot() {
        Turret[] turrets = new Turret[]{
                new Turret(42, 80, 0, -0.7853981633974483),
                new Turret(42, 80, 0, 0.7853981633974483),
                new Turret(42, 95, 0, -0.39269908169872414),
                new Turret(42, 95, 0, 0.39269908169872414),
                new Turret(42, 110, 0, 0)
        };
        ShootManager shootManager = new ShootManager(new int[]{0, 0, 1, 1, 2}, new float[]{0, 0.33f, 0.66f});
        return new BarrelManager(turrets, shootManager);
    }

    // Predator
    public static BarrelManager predator() {
        Turret[] turrets = new Turret[]{
                new Turret(42, 110, 0, 0),
                new Turret(56.7f, 95, 0, 0),
                new Turret(71.4f, 80, 0, 0)
        };
        ShootManager shootManager = new ShootManager(new int[]{0, 1, 2}, new float[]{0, 0.2f, 0.4f});
        return new BarrelManager(turrets, shootManager);
    }
}

/*
// Wait for the circle to be spawned before
// Triple shot
       turrets = new Turret[]{
                new Turret(42, 95, 0, -0.7853981633974483, scale),
                new Turret(42, 95, 0, 0, scale),
                new Turret(42, 95, 0, 0.7853981633974483, scale),
        };
        shootManager = new ShootManager(new int[]{0, 0, 0}, new int[]{16}, 1.0f);

// Twins (looks off?)
turrets = new Turret[]{
        new Turret(42f, 95, -26f, 0, scale),
                new Turret(42f, 95, 26f, 0, scale)
        };
reloadTime = (int) ((1.f/2) * Math.ceil((15 - 9)*1.0f) * 120 /25);

shootManager = new ShootManager(new int[]{0, 1}, new int[]{reloadTime, reloadTime}, 1.0f);

// Triplet
turrets = new Turret[]{
        new Turret(42, 80, 26, 0, scale),
                new Turret(42, 80, -26, 0, scale),
                new Turret(42, 95, 0, 0, scale)
        };
reloadTime = (int) ((1.f/2) * Math.ceil((15 - 9)*1.0f) * 120.f /25);
shootManager = new ShootManager(new int[]{1, 1, 0}, new int[]{reloadTime, reloadTime}, 1.0f);

// Pentashot
turrets = new Turret[]{
        new Turret(42, 80, 0, -0.7853981633974483, scale),
                new Turret(42 , 80, 0, 0.7853981633974483, scale),
                new Turret(42, 95, 0, -0.39269908169872414, scale),
                new Turret(42, 95, 0, 0.39269908169872414, scale),
                new Turret(42, 110, 0, 0, scale)
        };
reloadTime = (int) ((1.f/3) * Math.ceil((15 - 9)*1.0f) * 120 /25);
shootManager = new ShootManager(new int[]{0, 0, 1, 1, 2}, new int[]{reloadTime, reloadTime, reloadTime}, 1.0f);

// Predator
turrets = new Turret[]{
        new Turret(42, 110, 0, 0, scale),
                new Turret(1.35f * 42, 95, 0, 0, scale),
                new Turret(1.7f*42, 80, 0, 0, scale)
        };
reloadTime = (int) (Math.ceil((15 - 9)*3f) * 120 /25);
shootManager = new ShootManager(new int[]{0, 1, 2}, new int[]{reloadTime, (int) (reloadTime * 0.1f), (int) (reloadTime * 0.1f)}, 1.0f);


        // Single tank test

        turrets = new Turret[]{
                new Turret(42, 95, 0, 0, scale)
        };
        reloadTime = (int) (Math.ceil((15 - 9)*1) * 120 /25);
        shootManager = new ShootManager(new int[]{0}, new int[]{reloadTime}, 1.0f);


        // Fighter
        turrets = new Turret[]{
                new Turret(13.5f, 28, 0, 0, scale),
                new Turret(13.5f, 28, 0, 90, scale),
                new Turret(13.5f, 28, 0, -90, scale),
                new Turret(13.5f, 28,0, 150, scale),
                new Turret(13.5f, 28, 0, -150, scale)
        };
        shootManager = new ShootManager(new int[]{0, 0, 0, 0, 0}, new int[]{16}, 1.0f);


        // Destroyer
        turrets = new Turret[]{
                new Turret(1.7f * 42, 95, 0, 0, scale)
        };
        //ceil((15 - reload stat points) * base reload);
        reloadTime = (int) ((Math.ceil((15 - 9)*4.f*120.f /25)));
        shootManager = new ShootManager(new int[]{0}, new int[]{reloadTime}, 1.0f);
        */