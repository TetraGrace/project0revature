# this is just to build some random monsters with different stats that will dump them into a csv file
# for parsing the data into the SQL database. 
import random

def makeMonster(name,minH,maxH,minA,maxA,minD,maxD,diffRank):
    #this function makes monster strings to output to the csv file
    #minH: min Health, maxH: maxHealth etc.
    #values will be decided randomly in a range
    health = str(random.randint(minH,maxH))
    attack = str(random.randint(minA, maxA))
    defence = str(random.randint(minD,maxD))
    return name+','+diffRank+','+health+','+attack+','+defence




#content
print("Generating monsters...")
lines= []
#open thetext file containing the monster names
with open("I:\\try4\\hello-world\\src\\main\\scala\\databuilder\\monsternames.txt", "r") as f:
    lines = f.readlines()

# clean up the input a little
for i, item in enumerate(lines):
    
    lines[i] = item[:-1]

print(len(lines))
oList = []
count = 0
for name in lines:
    if count <50:
        maxHealth = 15
        minHealth = 1
        maxAttack = 10
        minAttack = 5
        maxDefense = 5
        minDefense = 0
        oList.append(makeMonster(name,minHealth,maxHealth,minAttack,maxAttack,minDefense,maxDefense, "easy"))
        count += 1
    elif count >= 50 and count < 100:
        maxHealth = 25
        minHealth = 10
        maxAttack = 20
        minAttack = 10
        maxDefense = 15
        minDefense = 5
        oList.append(makeMonster(name, minHealth, maxHealth,
                                 minAttack, maxAttack, minDefense, maxDefense, "medium"))
        count += 1
    elif count >= 100 and count <150:
        maxHealth = 50
        minHealth = 25
        maxAttack = 35
        minAttack = 15
        maxDefense = 25
        minDefense = 10
        oList.append(makeMonster(name, minHealth, maxHealth,
                                 minAttack, maxAttack, minDefense, maxDefense, "hard"))
        count += 1
    elif count >= 150 and count < 190:
        maxHealth = 85
        minHealth = 40
        maxAttack = 75
        minAttack = 35
        maxDefense = 75
        minDefense = 30
        oList.append(makeMonster(name, minHealth, maxHealth,
                                 minAttack, maxAttack, minDefense, maxDefense, "expert"))
        count += 1
    elif count >= 190 and count < 200:
        maxHealth = 155
        minHealth = 100
        maxAttack = 100
        minAttack = 50
        maxDefense = 100
        minDefense = 35
        oList.append(makeMonster(name, minHealth, maxHealth,
                                 minAttack, maxAttack, minDefense, maxDefense, "specail"))
        count += 1


fileA = open("I:\\try4\\hello-world\\src\\main\\scala\\databuilder\\monsters.csv", "w")
for mob in oList:
    fileA.write(mob + '\n')
fileA.close()


