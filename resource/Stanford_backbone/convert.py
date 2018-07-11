rtrs = ["bbra",
            "boza",
            "coza",
            "goza",
            "poza",
            "roza",
            "soza",
            "yoza",
            "bbrb",
                            "bozb",
                               "cozb",
                               "gozb",
                               "pozb",
                               "rozb",
                               "sozb",
                               "yozb"]

i= 0
for r in rtrs:
    f = open("%s_rtr.txt"%r, 'r')
    d = open("%s.txt"%r, 'w')
    c = open("%s_commands.txt"%r, 'w')
    c.write("table_set_default forward p4tester_forward\n")
    c.write("table_set_default record record_test %d\n"%i)
    i += 1
    count = 0
    for l in f:
        s = l.strip().split(' ')
        d.write("%s %d\n"%(s[0], count))
        c.write("table_add ipv4_lpm set_nhop %s => 10.0.0.1 %d\n"%(s[0], count))
        count += 1
        count %= 5
    f.close()
    d.close()