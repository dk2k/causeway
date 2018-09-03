package org.apache.isis.core.metamodel.adapter;

import java.util.UUID;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface ObjectAdapterProvider {

    /**
     * @return standalone (value) or root adapter
     */
    @Programmatic
    ObjectAdapter adapterFor(Object domainObject);

    /**
     * @return collection adapter.
     */
    @Programmatic
    ObjectAdapter adapterFor(
            final Object pojo,
            final ObjectAdapter parentAdapter,
            OneToManyAssociation collection);

    @Programmatic
    default ObjectSpecification specificationForViewModel(
            final SpecificationLoader specificationLoader, 
            final Object viewModelPojo) {
        
        //FIXME[ISIS-1976]
        // this is horrible, but there's a catch-22 here...
        // we need an adapter in order to query the state of the object via the metamodel, on the other hand
        // we can't create an adapter without the identifier, which is what we're trying to derive
        // so... we create a temporary transient adapter, use it to wrap this adapter and interrogate this pojo,
        // then throw away that adapter (remove from the adapter map)
        final boolean[] createdTemporaryAdapter = {false};
        final ObjectAdapter viewModelAdapter = adapterForViewModel(
                specificationLoader,
                viewModelPojo, 
                (ObjectSpecId objectSpecId)->{
                    createdTemporaryAdapter[0] = true;
                    return RootOid.create(objectSpecId, UUID.randomUUID().toString()); });

        final ObjectSpecification spec = viewModelAdapter.getSpecification();
        
        if(createdTemporaryAdapter[0]) {
            adapterManager().removeAdapterFromCache(viewModelAdapter);
        }
        return spec;
    }

    default ObjectAdapter adapterForViewModel(
            final SpecificationLoader specificationLoader,
            final Object viewModelPojo, 
            final Function<ObjectSpecId, RootOid> rootOidFactory) {

        ObjectAdapter viewModelAdapter = adapterManager().lookupAdapterFor(viewModelPojo);
        if(viewModelAdapter == null) {
            final ObjectSpecification objectSpecification = 
                    specificationLoader.loadSpecification(viewModelPojo.getClass());
            final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
            final RootOid newRootOid = rootOidFactory.apply(objectSpecId);

            viewModelAdapter = adapterManager().addRecreatedPojoToCache(newRootOid, viewModelPojo);
        }
        return viewModelAdapter;
    }
    
    @Deprecated // don't expose caching
    AdapterManager adapterManager();

}
