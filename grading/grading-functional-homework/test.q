
int isList(Q list){
    if(isNil(list)==1 || !isAtom(list)==1){
        return 1;
    }
    return 0;
}

Ref append(Ref list1, Ref list2){
    if(isNil(list1)!=0){
        return list2;
    }
    return left(list1) . append((Ref)right(list1),list2);
}

Ref reverse(Ref list){
    if(isNil(list)==1){
        return nil;
    }
    return append(reverse((Ref)right(list)), (Ref)(left(list) . nil));
}


int length(Ref list, int x){
    int check = x+1;
    if(isNil(list)==1){
        return check;
    }
    return length((Ref)right(list),check);
}

int isSorted(Ref list){
    if(isNil(list)==1 || isNil((Ref)right(list))==1){
        return 1;
    }
    if(length((Ref)left(list),0)<=length((Ref)right(list),0)){
        return isSorted((Ref)right(list));
    }
    return 0;
}

int sameLength(Ref list1, Ref list2){
    if(isNil(list1)==1 && isNil(list2)==1){
        return 1;
    }else if(isNil(list1)==1 || isNil(list2)==1){
        return 0;
    }
    return sameLength((Ref)right(list1),(Ref)right(list2));
}

int genericEquals(Q item1, Q item2) {
    if (isNil(item1) != isNil(item2)) {
        return 0;
    } else {
        if (isNil(item1) == 1) {
            return 1;
        }
    }
    if (isAtom(item1) != isAtom(item2)) {
        return 0;
    } else {
        if (isAtom(item1) == 1) {
            if ((int)item1 == (int)item2) { /* ??? */
                return 1;
            } else {
                return 0;
            }
        }
    }
    /* item1 and item2 are Ref's */
    if (genericEquals(left((Ref)item1), left((Ref)item2)) == 1 && genericEquals(right((Ref)item1), right((Ref)item2)) == 1) {
        return 1;
    }
    return 0;
}



int main(int arg) {
    Ref input1 = ((3 . 4) . (2 . 3) . nil);
    Ref input2 = (3 . (3 . 4 . 5). nil);
    if (sameLength(input1, input2) != 0) {
        return 1;  
    }
    return 0;  
}
