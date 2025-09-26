
int isList(Q list){
    if((isNil(list)==1 || !isAtom(list)==1)){
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
    if(length((Ref)left(list),0)<=length((Ref)left((Ref)right(list)),0)){
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