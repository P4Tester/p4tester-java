routes = [
    'bbra_rtr_route.txt', 'coza_rtr_route.txt', 'poza_rtr_route.txt', 'soza_rtr_route.txt',
    'bbrb_rtr_route.txt', 'cozb_rtr_route.txt', 'pozb_rtr_route.txt', 'sozb_rtr_route.txt',
    'boza_rtr_route.txt', 'goza_rtr_route.txt', 'roza_rtr_route.txt', 'yoza_rtr_route.txt',
    'bozb_rtr_route.txt', 'gozb_rtr_route.txt', 'rozb_rtr_route.txt', 'yozb_rtr_route.txt'
]
total = 0
count = 0

for r in routes:
    f = open(r)
    f.readline()
    count = 0
    for l in f:
        tmp = l.split(' ')
        n = []
        for t in tmp:
            if len(t) != 0 and t != '\n':
                n.append(t)
        if len(n) > 2 and n[1] != 'attached':
            count+= 1
    if count < 5000:
        total += count
    print r, count
print total