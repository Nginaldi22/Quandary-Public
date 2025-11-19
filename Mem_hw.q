mutable int main(int arg){
    if(arg==1){
        mutable Ref head = 1.nil;
        mutable Ref tail=head;
        mutable int x =0;
        while(x<16){
            mutable Ref temp = 1.nil;
            setRight(tail,temp);
            x=x+1;
            tail=temp;
        }
    }else if (arg==2){
        mutable Ref a = nil.nil;
        mutable Ref b = nil.nil;
        setRight(a,b);
        setRight(b,a);
        a=nil;
        b=nil;
        mutable int x=0;
        mutable Ref head = nil.nil; 
        mutable Ref tail = head;
     while(x<15){                  
        Ref temp = 1.nil;         
        setRight(tail,temp);      
        tail = temp;
        x=x+1;
    }
 }else if (arg==3){
        mutable Ref head = 1.nil;
        mutable Ref tail=head;
        mutable int x =0;
        while(x<16){
            mutable Ref temp = 1.nil;
            setRight(tail,temp);
            x=x+1;
            free tail;
            tail=temp;
        }
 }else if(arg==4){
        mutable int x=0;
        while(x<20){
            Ref temp = nil.nil;
            x=x+1;
        }
 }
    return 0;
}